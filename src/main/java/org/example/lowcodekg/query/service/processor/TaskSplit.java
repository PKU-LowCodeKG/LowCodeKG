package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.TaskGraph;

/**
 * @Description 将用户需求分解为子任务
 * @Author Sherloque
 * @Date 2025/3/22 20:54
 */
public interface TaskSplit {

    Result<TaskGraph> taskSplit(String query);

}
