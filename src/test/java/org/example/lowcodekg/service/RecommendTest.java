package org.example.lowcodekg.service;

import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.evaluation.DataProcess;
import org.example.lowcodekg.query.service.evaluation.Evaluate;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FilePrintStream;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.example.lowcodekg.query.utils.Constants.*;
import static org.example.lowcodekg.query.utils.FormatUtil.saveResult;

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
    @Autowired
    private Evaluate evaluate;

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
        FormatUtil.setPrintStream(logFilePath);

        String query = "实现商品SKU库存的CRUD(增删改查)管理功能";

        // 需求分解
        TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
        // 基于IR的需求-资源匹配并重排序
        for(Task task : taskGraph.getTasks().values()) {
            taskMatching.rerankResource(task);
        }
        // 任务合并
        Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();

//        saveResult(query, resourceList, SAVE_EM_RESULT_PATH);
    }

    @Test
    void testSingleQuery() {
        Task task = new Task();
        task.setName("添加商品收藏功能");
        task.setDescription("实现用户可以将商品添加到收藏列表的功能。");
        List<IR> irList = irGenerate.generateIR(
                task.getName() + ":" + task.getDescription(),
                "Workflow")
                .getData();
        for(IR ir : irList) {
            System.out.println("ir = " + ir.toString());
        }
        task.setIrList(irList);
        task.setIsWorkflow(true);
        task.setCategory(List.of("workflow"));

        List<Node> resourceList = templateRetrieve.queryBySubTask(task).getData();
        taskMatching.rerankResource(task);
    }

    @Test
    void testEmQueryList() {
        FormatUtil.setPrintStream(logFilePath);

        Map<String, List<String>> groundTruth = DataProcess.getQueryResultMap(EM_GROUND_TRUTH_JSON_FILE_PATH);
        for (Map.Entry<String, List<String>> entry : groundTruth.entrySet()) {
            String query = entry.getKey();
            try {
                TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
                for(Task task : taskGraph.getTasks().values()) {
                    taskMatching.rerankResource(task);
                }
                Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
                saveResult(query, resourceList, SAVE_EM_RESULT_PATH);
            } catch (Exception e) {
                System.out.println("query = " + query);
            }
        }
    }

    @Test
    void testBlogQueryList() {
//        FormatUtil.setPrintStream(logFilePath);

        // load data and run
        Map<String, List<String>> groundTruth = DataProcess.getQueryResultMap(BLOG_GROUND_TRUTH_JSON_FILE_PATH);
        for (Map.Entry<String, List<String>> entry : groundTruth.entrySet()) {
            String query = entry.getKey();
            System.out.println("testing query: " + query);
            try {
                TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
                for(Task task : taskGraph.getTasks().values()) {
                    taskMatching.rerankResource(task);
                }
                Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
                saveResult(query, resourceList, SAVE_BLOG_RESULT_PATH);
            } catch (Exception e) {
                System.out.println("query = " + query);
            }
        }

        // evaluate results
        FormatUtil.setPrintStream(BLOG_EVALUATE_RESULT_PATH);
        evaluate.evaluate(BLOG_GROUND_TRUTH_JSON_FILE_PATH, SAVE_BLOG_RESULT_PATH);
    }

    @Test
    void testEvaluate() {
        FormatUtil.setPrintStream(BLOG_EVALUATE_RESULT_PATH);
        evaluate.evaluate(BLOG_GROUND_TRUTH_JSON_FILE_PATH, SAVE_BLOG_RESULT_PATH);
    }
}
