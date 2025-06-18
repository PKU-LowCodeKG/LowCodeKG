package org.example.lowcodekg.service;

import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;

public interface FunctionalityGenService {


    /**
     * 工作流模块化处理，得到项目功能架构
     */
    void genWorkflowModule();

}
