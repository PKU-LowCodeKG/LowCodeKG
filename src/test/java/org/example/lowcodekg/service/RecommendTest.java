package org.example.lowcodekg.service;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.retriever.ElasticSearchService;
import org.example.lowcodekg.query.utils.FilePrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.example.lowcodekg.query.utils.Constants.*;

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

    @Test
    void testConfig() {
        String logFilePath = "D://Master//log.txt";
        FilePrintStream filePrintStream = null;
        try {
            filePrintStream = new FilePrintStream(logFilePath);
            System.setOut(filePrintStream); // 替换 System.out
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean debugMode = debugConfig.isDebugMode();
        System.out.println("debugMode = " + debugMode);
    }

//    @BeforeEach
    void setUp() throws IOException {
        esService.deleteIndex("test");;
        // 确保索引存在
        esService.createIndex(Document.class, PAGE_INDEX_NAME);
        esService.createIndex(Document.class, WORKFLOW_INDEX_NAME);
        esService.createIndex(Document.class, DATA_OBJECT_INDEX_NAME);
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
        List<Node> resourceList = taskMerge.mergeTask(taskGraph).getData();

        for(Node node : resourceList) {
            System.out.println(node.toString());
        }
    }

}
