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
    public Result<List<Node>> mergeTask(TaskGraph graph) {
        try {
            Set<Node> result = new HashSet<>();
            // 任务依赖图拓扑遍历
            List<Task> sortedTasks = graph.topologicalSort();
            for(Task task : sortedTasks) {
                if(task.getResourceList().size() == 0) {
                    continue;
                }
                // 获取当前任务的尾资源输出
                Node tailResource = task.getResourceList().get(task.getResourceList().size() - 1);

                // 获取当前任务的下游任务
                for(Map.Entry<Task, String> entry : graph.getDependencies(task.getId()).entrySet()) {
                    Task dependencyTask = entry.getKey();
                    String dependencyDescription = entry.getValue();
                    // 获取当前下游任务的首资源输入
                    Node headResource = dependencyTask.getResourceList().get(0);
                    // 判断上下游任务的输出-输入类型是否兼容

                }
                // 当前任务满足兼容性要求，加入结果集
                result.addAll(task.getResourceList());
            }

            if(debugConfig.isDebugMode()) {
                System.out.println("合并后的结果集:\n" + result + "\n");
            }
            List<Node> nodeList = new ArrayList<>(result);
            return Result.build(nodeList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in mergeTask: " + e.getMessage());
            throw new RuntimeException("Error in mergeTask: " + e.getMessage());
        }
    }
}
