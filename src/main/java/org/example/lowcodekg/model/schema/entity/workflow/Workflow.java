package org.example.lowcodekg.model.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流实体（代码实体及关系的集合）
 * 根据后端代码调用路径抽取功能实现逻辑
 * 跨项目执行聚类算法的对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    private String name;

    private String fullName;

    private String description;

    // 直接关联的method（工作流起点）
    private JavaMethod method;

    // 调用链中方法体内容拼接
    private String content;

    private String mappingUrl;

    // 请求响应方法调用链
    private List<JavaMethod> methodList = new ArrayList<>();

    // 工作流包含的子类别实体(聚类过程)
    private List<Workflow> subWorkflowList = new ArrayList<>();

    public Workflow(JavaMethod method) {
        this.name = method.getName();
        this.fullName = method.getFullName();
        this.method = method;
        this.mappingUrl = method.getMappingUrl();
        appendMethod(method);
    }

    public WorkflowEntity createWorkflowEntity(WorkflowRepo workflowRepo) {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setName(name);
        entity.setFullName(fullName);
        entity.setDescription(description);
        entity.setMappingUrl(mappingUrl);
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