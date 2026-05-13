package org.example.lowcodekg.query.service.processor.impl;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 21:02
 */
@Service
public class TaskMergeImpl implements TaskMerge {
    @Autowired
    private DebugConfig debugConfig;

    @Override
    public Result<Map<Task, Set<Node>>> mergeTask(TaskGraph graph, String query) {
        try {
            List<Task> sortedTasks = graph.topologicalSort();
            Map<Task, Set<Node>> result = new HashMap<>();
            for (Task task : sortedTasks) {
                result.put(task, new HashSet<>(task.getResourceList()));
            }

            if (debugConfig.isDebugMode()) {
                System.out.println("合并后的结果集:");
                for (Task task : result.keySet()) {
                    System.out.println("Task: " + task.getName() + ": " + task.getDescription());
                    for (Node node : result.get(task)) {
                        System.out.println("Node: " + node.getName() + ": " + node.getDescription());
                    }
                    System.out.println();
                }
            }

            return Result.build(result, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in mergeTask: " + e.getMessage());
            throw new RuntimeException("Error in mergeTask: " + e.getMessage());
        }
    }

}
