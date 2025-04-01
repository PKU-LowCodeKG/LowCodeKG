package org.example.lowcodekg.service;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.LLMService;
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
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private LLMService llmService;

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
//        String logFilePath = "/Users/chang/Documents/projects/dataset/log.txt";
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
                System.out.println("资源名称：" + node.getName()+ "\n资源描述：" + node.getDescription());
            }
            System.out.println("\n");
        }
    }

    @Test
    void testRetrieval() {

    }
}
