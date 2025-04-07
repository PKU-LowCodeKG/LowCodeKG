package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description 将子任务合并，并进行兼容性检查
 * @Author Sherloque
 * @Date 2025/3/22 21:01
 */
public interface TaskMerge {

    /**
     * 合并子任务，形成最终的资源推荐列表
     * @param graph
     * @return
     */
    Result<Map<Task, Set<Node>>> mergeTask(TaskGraph graph, String query);

    /**
     * 对重排后的子任务推荐资源列表进行决策
     * @param task
     * @return
     */
    Result<Void> rerankWithinTask(Task task);
}
