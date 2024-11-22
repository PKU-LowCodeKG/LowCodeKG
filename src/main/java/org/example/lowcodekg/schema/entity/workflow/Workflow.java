package org.example.lowcodekg.schema.entity.workflow;

import lombok.Data;

import java.util.List;

/**
 * 工作流逻辑，包含前后端的逻辑实现
 */
@Data
public class Workflow {

    private String name;

    private String description;

    /**
     * 前端脚本
     */
    private List<Script> scriptList;

    /**
     * 后端类
     */
    private List<JavaClass> classList;

    /**
     * 三方服务
     */
    private List<APIService> apiServiceList;
}
