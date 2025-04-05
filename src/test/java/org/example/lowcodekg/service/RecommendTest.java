package org.example.lowcodekg.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
import org.example.lowcodekg.query.service.util.LLMService;
import org.example.lowcodekg.query.service.util.ir.IRGenerate;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FilePrintStream;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.example.lowcodekg.query.utils.Constants.DATA_OBJECT_INDEX_NAME;

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
    private IRGenerate irGenerate;
    @Autowired
    private TemplateRetrieve templateRetrieve;
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

        String query = "我想要设置博客置顶状态，并添加对博客评论审核的功能";

        // 需求分解
        TaskGraph taskGraph = taskSplit.taskSplit(query).getData();

        // 基于IR的需求-资源匹配并重排序
        for(Task task : taskGraph.getTasks().values()) {
            System.out.println("task = " + task.toString());
            taskMatching.rerankResource(task);
        }

        // 任务合并
        Map<Task, List<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
    }

    @Test
    void testUnit() throws IOException {

    }
}
