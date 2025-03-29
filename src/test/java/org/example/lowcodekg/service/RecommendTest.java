package org.example.lowcodekg.service;

import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 测试需求推荐模板相关功能
 */
@SpringBootTest
public class RecommendTest {

    @Autowired
    private TaskSplit taskSplit;
    @Autowired
    private TaskMatching taskMatching;
    @Autowired
    private TaskMerge taskMerge;

    @Test
    public void test() {
        String query = "我希望实现一个用户注册的功能，能够通过邮箱注册一个新用户";

        // 检索增强的需求分解
        TaskGraph taskGraph = taskSplit.taskSplit(query).getData();

        // 基于IR的需求-资源匹配并重排序
        for(Task task : taskGraph.getTasks().values()) {
            taskMatching.rerankResource(task);
        }

        // 任务合并
        List<Node> resourceList = taskMerge.mergeTask(taskGraph).getData();

        for(Node node : resourceList) {
            System.out.println(node.toString());
        }

    }
}
