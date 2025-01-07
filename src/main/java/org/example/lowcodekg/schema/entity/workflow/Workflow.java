package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.WorkflowEntity;
import org.example.lowcodekg.dao.neo4j.repository.WorkflowRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流逻辑，包含前后端的逻辑实现
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    private String name;

    private String description;

    // 调用链中方法体内容拼接
    private String content;

    // 请求响应方法调用链
    private List<JavaMethod> methodList = new ArrayList<>();

    public Workflow(JavaMethod method) {
        this.name = method.getMappingUrl();
        this.methodList.add(method);
    }

    public WorkflowEntity createWorkflowEntity(WorkflowRepo workflowRepo) {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setName(name);
        entity.setDescription(description);
//        StringBuilder c = new StringBuilder();
//        for(JavaMethod method: methodList) {
//            c.append(method.getContent());
//            c.append("\n");
//        }
//        entity.setContent(c.toString());
//        List<String> mList = new ArrayList<>();
//        for(JavaMethod method: methodList) {
//            mList.add(method.getFullName());
//        }
//        entity.setMethodList(mList.toString());
        entity = workflowRepo.save(entity);
        return entity;
    }

}