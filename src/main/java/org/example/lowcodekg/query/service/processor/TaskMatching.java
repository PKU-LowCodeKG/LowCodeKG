package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;

import java.util.Map;

/**
 * @Description 实现子任务与资源的匹配
 * @Author Sherloque
 * @Date 2025/3/26
 */
public interface TaskMatching {

    /**
     * 子任务与资源的匹配度计算
     * @param task
     * @param node
     * @return
     */
    Result<Double> subTaskMatchingScore(Task task, Node node);

    /**
     * 对子任务的候选资源列表进行重排序
     * @param task
     * @return
     */
    Result<Void> rerankResource(Task task);
}
