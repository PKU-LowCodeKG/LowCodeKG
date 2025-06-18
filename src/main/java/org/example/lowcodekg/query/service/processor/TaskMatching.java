package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;

import java.util.List;
import java.util.Map;

/**
 * @Description 实现子任务与资源的匹配
 * @Author Sherloque
 * @Date 2025/3/26
 */
public interface TaskMatching {

    /**
     * 子任务与资源的匹配度计算，值越小，匹配度越高
     * 针对node类别进行路由，采取针对性的策略
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

    /**
     * 序列2转换为序列1的最低成本
     * @return
     */
    Double minTransformCost(List<IR> taskIRList, List<IR> templateIRList);
}
