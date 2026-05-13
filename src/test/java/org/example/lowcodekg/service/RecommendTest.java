package org.example.lowcodekg.service;

import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.evaluation.DataProcess;
import org.example.lowcodekg.query.service.evaluation.Evaluate;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private Evaluate evaluate;

    @Test
    void test() {
        FormatUtil.setPrintStream(logFilePath);

        String query = "实现商品SKU库存的CRUD(增删改查)管理功能";

        long start = System.currentTimeMillis();
        TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
        for (Task task : taskGraph.getTasks().values()) {
            taskMatching.rerankResource(task);
        }
        Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("\n========== 检索结果 ==========");
        System.out.println("查询: " + query);
        System.out.println("耗时: " + elapsed + "ms");
        for (Task task : resourceList.keySet()) {
            System.out.println("\n子任务: " + task.getName() + " | " + task.getDescription());
            for (Node node : resourceList.get(task)) {
                System.out.println("  -> " + node.getFullName() + " (" + node.getLabel() + ")");
            }
        }
    }

    @Test
    void testBlogQueryList() {
        Map<String, List<String>> groundTruth = DataProcess.getQueryResultMap(BLOG_GROUND_TRUTH_JSON_FILE_PATH);
        for (Map.Entry<String, List<String>> entry : groundTruth.entrySet()) {
            String query = entry.getKey();
            System.out.println("testing query: " + query);
            try {
                long start = System.currentTimeMillis();
                TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
                for (Task task : taskGraph.getTasks().values()) {
                    taskMatching.rerankResource(task);
                }
                Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("  耗时: " + elapsed + "ms");
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
