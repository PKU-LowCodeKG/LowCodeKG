package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Task;

import java.util.List;

/**
 * @Description 实现子任务与资源的匹配
 * @Author Sherloque
 * @Date 2025/3/26
 */
public interface TaskMatching {

    /**
     * 对单个子任务的候选资源列表进行重排序
     */
    Result<Void> rerankResource(Task task);

    /**
     * 批量处理：一次批量嵌入 + 并行 ES 检索 + 分配资源
     */
    Result<Void> rerankResources(List<Task> tasks);

}
