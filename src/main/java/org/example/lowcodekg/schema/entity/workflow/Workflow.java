package org.example.lowcodekg.schema.entity.workflow;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流逻辑，包含前后端的逻辑实现
 */
@Data
public class Workflow {

    private String name;

    private String description;

    /**
     * 调用链中方法体内容拼接
     */
    private String content;

    /**
     * 请求响应方法调用链
     */
    private List<JavaMethod> methodList = new ArrayList<>();

}
