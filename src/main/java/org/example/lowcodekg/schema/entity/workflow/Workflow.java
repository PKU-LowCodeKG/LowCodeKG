package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.WorkflowEntity;
import org.example.lowcodekg.dao.neo4j.repository.WorkflowRepo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 工作流实体（代码实体及关系的集合）
 * 根据后端代码调用路径抽取功能实现逻辑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    private String name;

    private String description;

    // 直接关联的method（工作流起点）
    private JavaMethod method;

    // 调用链中方法体内容拼接
    private String content;

    // 请求响应方法调用链
    private List<JavaMethod> methodList = new ArrayList<>();

    public Workflow(JavaMethod method) {
        this.name = method.getMappingUrl();
        this.method = method;
        appendMethod(method);
    }

    public WorkflowEntity createWorkflowEntity(WorkflowRepo workflowRepo) {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity = workflowRepo.save(entity);
        return entity;
    }

    private void appendMethod(JavaMethod method) {
//        Queue<JavaMethod> q = new LinkedList();
//        q.add(method);
//        while(!q.isEmpty()) {
//            JavaMethod cur = q.poll();
//            methodList.add(cur);
//            for(JavaMethod m: cur.getMethodCallList()) {
//                q.add(m);
//            }
//        }
        methodList.add(method);
        for(JavaMethod m: method.getMethodCallList()) {
            appendMethod(m);
        }
    }
}