package org.example.lowcodekg.service;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.classification.BertTextProcessor;
import org.example.lowcodekg.query.service.classification.WekaTextProcessor;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.retriever.ElasticSearchService;
import org.example.lowcodekg.query.utils.FilePrintStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;

/**
 * 测试需求推荐模板相关功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class RecommendTest {

    @Autowired
    private TaskSplit taskSplit;
    @Autowired
    private TaskMatching taskMatching;
    @Autowired
    private TaskMerge taskMerge;
    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private DebugConfig debugConfig;

//    @BeforeEach
    @Test
    void setUp() throws IOException {
//        esService.deleteIndex("test");;
//        esService.deleteAllIndices();
        // 确保索引存在
        esService.createDefaultIndex();
    }

    @Test
    void test() {
        // 设置输出到本地日志文件
        String logFilePath = "D://Master//log.txt";
        FilePrintStream filePrintStream = null;
        try {
            filePrintStream = new FilePrintStream(logFilePath);
            System.setOut(filePrintStream); // 替换 System.out
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String query = "我想要能够更新博客的置顶文章的状态";

        // 检索增强的需求分解
        TaskGraph taskGraph = taskSplit.taskSplit(query).getData();

        // 基于IR的需求-资源匹配并重排序
        for(Task task : taskGraph.getTasks().values()) {
            System.out.println("task = " + task.toString());
            taskMatching.rerankResource(task);
        }

        // 任务合并
//        List<Node> resourceList = taskMerge.mergeTask(taskGraph).getData();

        System.out.println("推荐资源结果列表:");
        List<Task> sortedTasks = taskGraph.topologicalSort();
        for(Task task : sortedTasks) {
            if(task.getResourceList().size() == 0) {
                continue;
            }
            System.out.println("当前任务：" + task.getName());
            for(Node node : task.getResourceList()) {
                System.out.println("资源名称：" + node.getName());
                System.out.println("资源描述：" + node.getDescription());
            }
            System.out.println("\n");
        }
    }

    @Test
    void testClassification() throws Exception {
        String query = "新增博客实体的置顶状态字段";
        WekaTextProcessor processor = new WekaTextProcessor();
        processor.loadModel();

        List<String> predictedLabels = processor.predict(query);
        System.out.println("查询文本: " + query);
        System.out.println("预测标签: " + predictedLabels);
    }
    
    @Test
    void testBertClassification() throws Exception {
        // 测试BERT文本分类器
        String query = "新增博客实体的置顶状态字段";
        
        // 导入BERT分类器
        BertTextProcessor processor =
            new BertTextProcessor();
        
        // 尝试加载已有模型，如果不存在则会抛出异常
        try {
            processor.loadModel();
            System.out.println("成功加载BERT模型");
        } catch (Exception e) {
            System.out.println("加载BERT模型失败，可能需要先训练模型: " + e.getMessage());
            return;
        }
        
        // 测试多个查询
        String[] queries = {
            "新增博客实体的置顶状态字段",
            "创建一个新的数据表用于存储客户信息",
            "设计一个工作流程用于审批请假申请",
            "实现一个用户登录界面，包含用户名密码输入框和登录按钮",
            "开发数据统计报表页面，展示销售数据分析结果"
        };
        
        System.out.println("\n=== BERT分类器测试结果 ===\n");
        
        for (String testQuery : queries) {
            List<String> predictedLabels = processor.predict(testQuery);
            System.out.println("查询文本: " + testQuery);
            System.out.println("预测标签: " + String.join(", ", predictedLabels));
            System.out.println("-------------------");
        }
    }
}
