# 两阶段检索流程

`/recommend?query=...` 接口采用两阶段检索，全程走 Elasticsearch，Neo4j 仅在写入链路（图谱构建）使用。

## 架构

```
用户查询 → 阶段一(模板匹配, ES) → 命中 → 子任务列表 → 阶段二(资源检索, ES) → 合并返回
                                  ↓ 未命中
                                  原始 query 直接 → 阶段二
```

## 阶段一：需求分解（任务模板匹配）

将用户查询与任务模板库做语义匹配，找到对应的子任务分解方案。

### 数据来源

`src/main/resources/data/blog_output.json`，包含 23 个任务模板：

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

### 索引方式

启动时 `SubTaskIndexService.init()` 将 23 个模板各写一条 ES 文档：
- 搜索字段仅用 `query` 文本做向量化，不含子任务细节（提高匹配精度）
- 模板 ID → 子任务列表的映射保留在内存 `Map<Integer, List<Document>>` 中

### 匹配逻辑

`SubTaskIndexService.searchSubTasks(query)`：
1. 用户查询 → 向量 + 文本混合搜索 ES 模板索引
2. 取 1 个最匹配模板（`MAX_SUBTASK_NUM = 1`）
3. 分数低于 `SUBTASK_TEMPLATE_THRESHOLD = 0.65` → 视为未命中，返回空列表
4. 命中 → 从内存 map 取出该模板的子任务列表返回

### Fallback

`TaskSplitImpl.taskSplit()` 中若模板未命中：
- 创建单个 Task（name=原始query, description=原始query）
- 直接进入阶段二

### 关键类

| 类 | 职责 |
|---|---|
| `SubTaskIndexService` | 启动时构建模板索引、运行时匹配模板 |
| `TaskSplitImpl` | 编排阶段一，处理 fallback 路径 |

## 阶段二：资源匹配

用阶段一产出的子任务列表，逐一检索对应的代码资源（页面、接口、数据对象）。

### 匹配逻辑

`TaskMatchingImpl.rerankResource(task)` 对每个子任务：
1. 拼接 `name + " " + description` 作为查询文本
2. 纯向量搜索 ES 代码实体索引（`CODE_ENTITY_INDEX_NAME`）
3. 分数 ≥ `MIN_SCORE = 0.9` 的资源才保留
4. 每个子任务最多保留 `MAX_RESOURCE_RECOMMEND_NUM = 1` 个
5. 无匹配资源 → 跳过该子任务

### 关键类

| 类 | 职责 |
|---|---|
| `TaskMatchingImpl` | 遍历子任务逐一检索，过滤空结果 |
| `TemplateRetrieveImpl` | 调用 ES 混合搜索，将 Document 转为 Node 对象 |

## 结果合并

`TaskMergeImpl` 对 TaskGraph 做拓扑排序后，将所有子任务的资源集合去重合并为 `List<Node>`。

## 关键常量

定义位置：`src/main/java/.../query/utils/Constants.java`

| 常量 | 值 | 作用 |
|---|---|---|
| `MAX_SUBTASK_NUM` | 1 | 阶段一返回模板数 |
| `SUBTASK_TEMPLATE_THRESHOLD` | 0.65 | 阶段一模板匹配最低分数 |
| `MAX_CODE_ENTITY_NUM` | 30 | 阶段二候选资源上限 |
| `MIN_SCORE` | 0.9 | 阶段二资源匹配最低分数 |
| `MAX_RESOURCE_RECOMMEND_NUM` | 1 | 阶段二每个子任务最终保留资源数 |

## 关于 Neo4j

Neo4j 仅在知识图谱构建阶段（`-gen`）写入实体及图关系。检索链路 `/recommend` 全程走 Elasticsearch。代码中 `Node.java` 和 `convertToNeo4jNode()` 方法名有误导性——它们仅从 ES Document 创建 Java POJO，不涉及 Neo4j 查询。

## 数据流总览

```
启动时:
  blog_output.json → SubTaskIndexService → ES 模板索引(23条) + 内存映射

构建阶段 (-gen):
  Java源码/前端页面/组件文档 → KnowledgeExtractor → Neo4j + ES 代码实体索引

查询时 (/recommend):
  用户查询 → SubTaskIndexService.searchSubTasks()
           → ES 模板索引(向量+文本混合搜索, top-1)
           → 命中? → 子任务列表 : fallback原始query
           → TaskMatchingImpl.rerankResource()
           → ES 代码实体索引(纯向量搜索, top-1 per subtask)
           → TaskMergeImpl → List<Node>
```

## 日志示例

```
==== 阶段一：需求分解 ====
查询: 实现博客文章置顶的功能
命中模板: 实现博客文章置顶的功能
  子任务 1: 在文章编辑页面添加置顶选项 | 在前端表单组件中添加一个选项...
  子任务 2: 创建置顶状态更新接口 | 定义一个API接口，接收博客ID和置顶状态...
  子任务 3: 添加文章置顶字段到数据库 | 在博客文章实体类中添加一个布尔类型字段...
  子任务 4: 在博客列表页面显示置顶状态 | 在博客管理界面的表格中增加一列...
  子任务 5: 实现置顶逻辑 | 在服务层中创建方法，处理文章置顶状态的更新...
  子任务 6: 按置顶状态排序博客列表 | 修改博客列表查询逻辑，使置顶文章优先显示...
==== 阶段二：资源匹配 ====
[在文章编辑页面添加置顶选项] 匹配 1 个资源:
  - WriteBlog (PageTemplate)
[创建置顶状态更新接口] 匹配 1 个资源:
  - BlogAdminController (Workflow)
[添加文章置顶字段到数据库] 匹配 1 个资源:
  - Blog (DataObject)
[在博客列表页面显示置顶状态] 匹配 1 个资源:
  - BlogList (PageTemplate)
[实现置顶逻辑] 匹配 1 个资源:
  - BlogAdminController (Workflow)
[按置顶状态排序博客列表] 匹配 1 个资源:
  - BlogController (Workflow)
```
