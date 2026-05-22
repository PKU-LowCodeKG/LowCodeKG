package org.example.lowcodekg.query.service.util.retriever;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;

import java.util.List;
import java.util.Map;

/**
 * @Description 根据查询，检索库中相关模板资源
 * @Author Sherloque
 * @Date 2025/3/22 20:56
 */
public interface TemplateRetrieve {

    /**
     * 根据用户整体需求从库中查询相关的模板资源
     */
    Result<List<Node>> queryByTask(String query);

    /**
     * 根据子任务描述从库中查询相关资源
     */
    Result<List<Node>> queryBySubTask(Task task);

    /**
     * 批量查询：一次批量嵌入 + 并行 ES 检索
     */
    Result<Map<Task, List<Node>>> queryBySubTasks(List<Task> tasks);

}
