package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;

/**
 * @Description 实现子任务与资源的匹配
 * @Author Sherloque
 * @Date 2025/3/26
 */
public interface TaskMatching {

    /**
     * 对子任务的候选资源列表进行重排序
     * @param task
     * @return
     */
    Result<Void> rerankResource(Task task);

}
