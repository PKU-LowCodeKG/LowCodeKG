package org.example.lowcodekg.query.service.util.summarize;

import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.model.result.Result;

/**
 * @Description 为低代码资源生成功能描述
 * @Author Sherloque
 * @Date 2025/3/23 15:47
 */
public interface FuncGenerate {

    /**
     * 针对单个页面，生成功能描述信息
     */
    void genPageFunc(PageEntity pageEntity);

    /**
     * 针对单个工作流，生成功能描述信息
     */
    void genWorkflowFunc(WorkflowEntity workflowEntity);

    /**
     * 针对数据实体，生成功能描述信息
     */
    void genDataObjectFunc(JavaClassEntity classEntity);
}
