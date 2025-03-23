package org.example.lowcodekg.query.service.retriever;

import org.example.lowcodekg.model.dto.Neo4jNode;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Node;

import java.util.List;

/**
 * @Description 根据查询，检索库中相关模板资源
 * @Author Sherloque
 * @Date 2025/3/22 20:56
 */
public interface TemplateRetrieve {

    /**
     * 根据用户整体需求从库中查询相关的模板资源
     * @param query
     * @return
     */
    Result<List<Node>> queryEntitiesByTask(String query);

    /**
     * 根据用户需求从库中查询相关的模板资源
     * @param subQuery
     * @return
     */
    Result<List<Node>> queryEntitiesBySubTask(String subQuery);

}
