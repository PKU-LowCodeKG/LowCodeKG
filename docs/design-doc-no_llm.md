# no_llm 分支设计文档

> 作者: jasmine  
> 分支: `no_llm` (基于 `master`)  
> 日期: 2026-05-22  

---

## 1. 分支目标

将 **查询推荐链路** 中的 LLM 依赖完全移除，改为基于 Elasticsearch 的两阶段检索方案，使推荐接口 `/recommend` 在无需 LLM 调用的情况下运行，降低延迟和外部依赖。

LLM 仅保留用于 **离线描述生成**（知识图谱构建阶段），不参与在线查询。

---

## 2. 架构总览

```
┌──────────────────────────────────────────────────────────────────────┐
│                        在线查询链路 (/recommend)                       │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  用户查询                                                             │
│    │                                                                 │
│    ▼                                                                 │
│  ┌─────────────────────────────────────┐                             │
│  │ 阶段一：需求分解 (TaskSplitImpl)      │                             │
│  │  SubTaskIndexService.searchSubTasks()│                             │
│  │  ES 模板索引 (向量+文本混合搜索)       │                             │
│  └────────────┬───────────────┬────────┘                             │
│        命中    │               │ 未命中                                │
│               ▼               ▼                                      │
│        子任务列表         原始query作为                                │
│               │           单个fallback任务                            │
│               └───────┬───────┘                                      │
│                       ▼                                              │
│  ┌─────────────────────────────────────┐                             │
│  │ 阶段二：资源匹配 (TaskMatchingImpl)    │                             │
│  │  批量嵌入 + 并行ES向量搜索             │                             │
│  │  TemplateRetrieveImpl.queryBySubTasks│                             │
│  └────────────────────┬────────────────┘                             │
│                       ▼                                              │
│  ┌─────────────────────────────────────┐                             │
│  │ 阶段三：任务合并 (TaskMergeImpl)       │                             │
│  │  拓扑排序 + 去重                      │                             │
│  └────────────────────┬────────────────┘                             │
│                       ▼                                              │
│                 List<Node> 返回                                       │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                    离线构建链路 (KnowledgeExtractorService)             │
├──────────────────────────────────────────────────────────────────────┤
│  源码/页面文件 → KnowledgeExtractor → Neo4j 写入                       │
│                                    → DescriptionGenerator (LLM)      │
│                                    → FuncGenerateImpl → ES 代码实体索引│
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. 与 master 分支的核心差异

| 维度 | master | no_llm |
|------|--------|--------|
| 查询时任务分解 | LLM (IRGenerate) 生成中间表示 | ES 模板匹配 (SubTaskIndexService) |
| 查询时资源检索 | 经 LLM rerank | 纯 ES 向量搜索 + 分数阈值过滤 |
| LLM 策略 | 多策略: Deepseek/Ollama/Zhipu ChatStrategy | 仅保留 DeepSeek (离线描述生成) |
| 外部依赖(查询时) | LLM API + ES | 仅 ES |
| 延迟 | 秒级 (LLM调用) | 毫秒级 (纯ES) |

### 3.1 删除的模块

| 文件/类 | 原功能 |
|---------|--------|
| `LLMConfig` | LLM 模型类型配置 |
| `IRGenerate` / `IRGenerateImpl` | LLM 生成中间表示(IR) |
| `LLMGenerateService` / `LLMGenerateServiceImpl` | 上层 LLM 调用封装 |
| `ChatStrategy` 接口及实现 (Deepseek/Ollama/Zhipu) | 多 LLM 策略模式 |
| `ApiKeyUtil` | API Key 轮转工具 |
| `IR` model class | 中间表示数据模型 |

### 3.2 新增的模块

| 文件/类 | 功能 |
|---------|------|
| `SubTaskIndexService` | 启动时构建任务模板ES索引；运行时匹配模板返回子任务 |
| `DescriptionGenerator` | 离线批量调用 LLM 为 Neo4j 实体生成功能描述 |
| `SearchBenchmarkTest` | 20条查询的端到端性能基准测试 |
| `blog_output.json` | 23 个任务模板数据 |
| `descriptions.json` | LLM 生成的实体描述缓存 |
| `logback-spring.xml` | 结构化日志配置 |

### 3.3 重构的模块

| 文件/类 | 变更说明 |
|---------|----------|
| `TaskSplitImpl` | 改为调用 SubTaskIndexService 做模板匹配，不再调 LLM |
| `TaskMatchingImpl` | 新增 `rerankResources()` 批量接口；移除 LLM rerank |
| `TemplateRetrieveImpl` | 新增 `queryBySubTasks()` 批量嵌入+并行ES查询 |
| `ElasticSearchService` | 新增 `hybridSearch()` 混合检索(k-NN + BM25) |
| `LLMService` | 精简为单一 DeepSeek 实现 (langchain4j OpenAiChatModel) |
| `MainServiceImpl` | 流程编排适配新接口 |
| `FuncGenerateImpl` | 适配新的描述生成流程 |

---

## 4. 核心组件详细设计

### 4.1 SubTaskIndexService (任务模板索引)

**职责**: 将预定义的任务模板索引到 ES，查询时返回最匹配模板的子任务列表。

**数据源**: `src/main/resources/data/blog_output.json`

```json
{
  "predicted": [
    {
      "query": "实现博客文章置顶的功能",
      "task": [
        {"name": "在文章编辑页面添加置顶选项", "description": "..."},
        {"name": "创建置顶状态更新接口", "description": "..."}
      ]
    }
  ]
}
```

**初始化流程** (`@PostConstruct init()`):
1. 删除并重建 ES 索引 `subtask`
2. 加载 JSON 模板文件
3. 每个模板的 `query` 字段做嵌入向量化后写入 ES
4. 子任务列表存储在内存 `Map<Integer, List<Document>>`

**查询流程** (`searchSubTasks(query)`):
1. 用户查询 → BgeSmallZhV15 嵌入
2. ES 混合搜索 (向量权重0.7 + 文本权重0.3)，取 top-1
3. 分数 < `SUBTASK_TEMPLATE_THRESHOLD` (0.5) → 返回空列表
4. 命中 → 从内存 map 取出子任务列表

### 4.2 ElasticSearchService (搜索服务)

**核心方法**: `hybridSearch(textQuery, vectorQuery, maxResults, minScore, textWeight, indexName)`

两种模式：
- `textWeight <= 0.01`: 纯 k-NN 向量搜索 (HNSW 索引，O(log N))
- `textWeight > 0.01`: k-NN + BM25 多字段混合搜索

**索引配置**:
- 向量维度: 512 (BgeSmallZhV15 模型输出)
- 相似度函数: cosine
- 文本分析器: standard

### 4.3 TemplateRetrieveImpl (资源检索)

**单任务查询** (`queryBySubTask`): 拼接 `name + description` → 嵌入 → 纯向量搜索 ES code_entity 索引

**批量查询** (`queryBySubTasks`): 
1. 所有子任务文本一次批量嵌入 (`EmbeddingUtil.embedTexts()`)
2. `CompletableFuture` 并行发起 ES 查询
3. 等待全部完成后收集结果

### 4.4 EmbeddingUtil (嵌入工具)

**模型**: `BgeSmallZhV15QuantizedEmbeddingModel` (ONNX 本地推理，无网络依赖)
- 维度: 512
- 支持批量嵌入 (`embedTexts`)
- 无需外部 API 调用

### 4.5 DescriptionGenerator (离线描述生成)

**调用时机**: 知识图谱构建结束后自动触发 (`KnowledgeExtractorService.execute()` 末尾)

**处理流程**:
1. 遍历所有 JavaClass / JavaMethod / Workflow / Page 实体
2. 调用 DeepSeek LLM 生成功能描述
3. 更新 Neo4j 实体的 `description` 字段
4. 调用 `FuncGenerate` 将描述写入 ES code_entity 索引
5. 所有描述缓存到 `descriptions.json`

**LLM 配置** (application.properties):
- API: DeepSeek (`https://api.deepseek.com`)
- Model: `deepseek-chat`
- Temperature: 0.1
- Max tokens: 4096

---

## 5. 数据流

### 5.1 启动时自动执行

```
blog_output.json
    → SubTaskIndexService.init()
    → ES "subtask" 索引 (23条模板文档)
    → 内存 Map<Integer, List<Document>> (模板ID→子任务列表)
```

### 5.2 知识图谱构建 (手动触发)

```
配置YAML → KnowledgeExtractorService.execute()
    → 清空 Neo4j + ES
    → KnowledgeExtractor.execute() (解析源码/页面→写入Neo4j)
    → DescriptionGenerator.generateAll() (LLM 生成描述→更新Neo4j+ES)
```

### 5.3 在线查询 (/recommend?query=...)

```
用户查询
  → TaskSplitImpl.taskSplit()
      → SubTaskIndexService.searchSubTasks() [ES 混合搜索, <100ms]
      → 命中? → 子任务列表 : fallback(原始query)
  → TaskMatchingImpl.rerankResources()
      → EmbeddingUtil.embedTexts() [批量本地嵌入, <50ms]
      → TemplateRetrieveImpl.queryBySubTasks() [并行ES向量搜索, <200ms]
      → 分数过滤 (MIN_SCORE=0.5) + 截断 (MAX_RESOURCE_RECOMMEND_NUM=1)
  → TaskMergeImpl.mergeTask()
      → 拓扑排序 + 去重
  → List<Node> 返回
```

---

## 6. 关键常量

定义位置: `src/main/java/.../query/utils/Constants.java`

| 常量 | 值 | 说明 |
|------|------|------|
| `CODE_ENTITY_INDEX_NAME` | `"code_entity"` | ES 代码实体索引名 |
| `SUBTASK_INDEX_NAME` | `"subtask"` | ES 任务模板索引名 |
| `MAX_CODE_ENTITY_NUM` | 10 | 每次 ES 查询最大返回数 |
| `MAX_SUBTASK_NUM` | 1 | 阶段一匹配模板数 |
| `MIN_SCORE` | 0.5 | 阶段二资源匹配最低分数 |
| `SUBTASK_TEMPLATE_THRESHOLD` | 0.5 | 阶段一模板匹配最低分数 |
| `MAX_RESOURCE_RECOMMEND_NUM` | 1 | 每个子任务最终保留资源数 |

---

## 7. 外部依赖

### 7.1 运行时必须

| 服务 | 用途 | 备注 |
|------|------|------|
| Elasticsearch 8.x | 向量搜索 + 文本搜索 | 端口 9200 |
| Neo4j 5.x | 知识图谱存储 | 端口 7687 |

### 7.2 构建时可选

| 服务 | 用途 | 备注 |
|------|------|------|
| DeepSeek API | 离线描述生成 | 仅图谱构建阶段需要 |

### 7.3 查询时无需

- 无 LLM 调用
- 无外部嵌入 API (本地 ONNX 模型)

---

## 8. 技术栈

| 组件 | 版本/说明 |
|------|-----------|
| Java | 17 |
| Spring Boot | 3.3.4 |
| Spring Data Neo4j | (Boot managed) |
| Spring Data Elasticsearch | (Boot managed) |
| langchain4j | OpenAiChatModel (DeepSeek compatible) |
| langchain4j-embeddings | BgeSmallZhV15 ONNX Quantized |
| fastjson | JSON 解析 |

---

## 9. 接口说明

### GET /recommend

**参数**: `query` (String) - 用户自然语言需求描述

**返回**: `Result<List<Node>>`

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 123,
      "name": "WriteBlog",
      "fullName": "com.blog.page.WriteBlog",
      "label": "PageTemplate",
      "description": "博客文章编写页面"
    }
  ]
}
```

**性能**: 典型查询 < 500ms (批量基准测试20条查询)

---

## 10. 配置说明

`src/main/resources/application.properties`:

```properties
# 调试模式（开启后输出详细检索日志）
app.debug.mode=false

# Neo4j
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=neo4j123456

# Elasticsearch
spring.elasticsearch.host=http://localhost
spring.elasticsearch.port=9200
spring.elasticsearch.username=elastic
spring.elasticsearch.password=<your-password>

# DeepSeek (仅离线描述生成需要)
deepseek.api.key=<your-key>
deepseek.base.url=https://api.deepseek.com
deepseek.model=deepseek-chat
deepseek.temperature=0.1
deepseek.max.tokens=4096
```

---

## 11. 本地运行指南

### 前置条件

1. Java 17+
2. Elasticsearch 8.x 运行在 localhost:9200
3. Neo4j 5.x 运行在 localhost:7687
4. (可选) DeepSeek API Key（仅图谱构建需要）

### 启动步骤

```bash
# 1. 启动 ES 和 Neo4j
# 2. 编译运行
mvn spring-boot:run

# 启动后自动执行：
#   - SubTaskIndexService.init() 构建模板索引
#   - 服务监听 8081 端口
```

### 知识图谱构建

通过测试用例或 API 触发 `KnowledgeExtractorService.execute(yamlConfig)`，会：
1. 清空 Neo4j 和 ES
2. 解析源码构建知识图谱
3. 调用 LLM 生成描述（需要 DeepSeek API Key）
4. 将描述索引到 ES

### 测试查询

```bash
curl "http://localhost:8081/recommend?query=实现博客文章置顶的功能"
```

### 性能基准测试

```bash
mvn test -Dtest=SearchBenchmarkTest -pl .
```

---

## 12. 已知限制与后续可改进点

1. **模板库规模**: 当前仅 23 个模板 (blog_output.json)，覆盖面有限。未命中模板时 fallback 为原始 query 直接检索，匹配精度下降。
2. **常量硬编码路径**: `Constants.java` 中文件路径为 Windows 绝对路径 (`D:\\Master\\...`)，跨平台需修改。
3. **MAX_RESOURCE_RECOMMEND_NUM=1**: 每个子任务仅返回1个资源，可能丢失相关结果。可根据业务需要调整。
4. **嵌入模型**: BgeSmallZhV15 (512维) 为轻量模型，对于复杂语义匹配可能精度不足。可升级为更大模型。
5. **Neo4j 数据文件**: 分支中包含了 `neo4j/data/` 二进制文件，建议后续通过 `.gitignore` 排除。
6. **SubTaskIndexService 启动时重建索引**: 每次启动都会删除重建 subtask 索引，若模板数据未变可优化为增量。

---

## 13. 文件目录结构 (核心)

```
src/main/java/org/example/lowcodekg/
├── controller/
│   └── QueryController.java              # /recommend 入口
├── query/
│   ├── model/
│   │   ├── Node.java                     # 资源节点 POJO
│   │   ├── Task.java                     # 子任务模型
│   │   └── TaskGraph.java                # 任务图（支持拓扑排序）
│   ├── service/
│   │   ├── llm/
│   │   │   └── LLMService.java           # DeepSeek 调用（仅离线用）
│   │   ├── processor/
│   │   │   ├── MainService.java          # 编排接口
│   │   │   ├── TaskSplit.java            # 阶段一接口
│   │   │   ├── TaskMatching.java         # 阶段二接口
│   │   │   ├── TaskMerge.java            # 阶段三接口
│   │   │   └── impl/
│   │   │       ├── MainServiceImpl.java
│   │   │       ├── TaskSplitImpl.java
│   │   │       ├── TaskMatchingImpl.java
│   │   │       └── TaskMergeImpl.java
│   │   └── util/
│   │       ├── ElasticSearchService.java # ES 搜索封装
│   │       ├── EmbeddingUtil.java        # 本地嵌入 (BgeSmallZhV15)
│   │       ├── SubTaskIndexService.java  # 任务模板索引
│   │       ├── retriever/
│   │       │   ├── TemplateRetrieve.java
│   │       │   └── TemplateRetrieveImpl.java
│   │       └── summarize/
│   │           ├── DescriptionGenerator.java  # 离线 LLM 描述生成
│   │           ├── FuncGenerate.java
│   │           └── FuncGenerateImpl.java
│   └── utils/
│       ├── Constants.java                # 全局常量
│       ├── FormatUtil.java               # 格式转换工具
│       └── Prompt.java                   # LLM prompt 模板
├── service/
│   ├── KnowledgeExtractorService.java    # 图谱构建编排
│   └── impl/
│       └── ClineServiceImpl.java         # Cline 服务(mock)
└── extraction/                           # 源码/页面解析器
    ├── KnowledgeExtractor.java
    ├── page/PageExtractor.java
    └── workflow/JavaExtractor.java

src/main/resources/
├── application.properties
├── logback-spring.xml
└── data/
    ├── blog_output.json                  # 任务模板库 (23条)
    └── descriptions.json                 # LLM 描述缓存

src/test/java/.../service/
├── SearchBenchmarkTest.java              # 性能基准测试
├── RecommendTest.java                    # 推荐功能测试
└── PromptTest.java                       # 描述生成测试
```

---

## 14. Commit 历史

| Commit | 说明 |
|--------|------|
| `89e398d` | 移除查询链路中的 LLM 和 IR 依赖 |
| `0d25227` | 清理 neo4j 事务日志大文件 |
| `fde5580` | 实现两阶段检索架构 |
| `e8909ef` | 新增离线 LLM 描述生成 (DescriptionGenerator) |
