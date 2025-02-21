package org.example.lowcodekg.extraction.service;

import org.example.lowcodekg.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;

public interface FunctionalityGenService {

    /**
     * 针对单个页面，生成功能描述信息
     */
    void generatePageFunctionality(PageEntity pageEntity);

    /**
     * 针对单个工作流，生成功能&技术描述信息
     */
    void genWorkflowFunc(WorkflowEntity workflowEntity);

    /**
     * 工作流模块化处理，得到项目功能架构
     */
    void genWorkflowModule();

}
