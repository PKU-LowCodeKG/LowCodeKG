package org.example.lowcodekg.extraction.workflow;

import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

/**
 * 从请求响应方法出发，抽取出该功能实现的方法调用链以及相关的数据实体
 * @return 工作流实体
 * @author Sherloque
 * @date 2025/3/15
 */
public class WorkflowExtractor extends KnowledgeExtractor {

    @Override
    public void extraction() {
        try {
            String cypher = """
                    MATCH (n:Workflow)
                    RETURN n
                    """;
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(cypher);
            while(result.hasNext()) {
                Node node = result.next().get("n").asNode();
                Optional<WorkflowEntity> optional = workflowRepo.findById(node.id());
                optional.ifPresent(workflowEntity -> {
                    // 获取入口方法实体
                    JavaMethodEntity startMethodEntity = getStartMethodEntity(workflowEntity);
                    if(!Objects.isNull(startMethodEntity)) {
                        getMethodInvokeChain(workflowEntity, startMethodEntity, 0);

                        // 将方法和数据对象内容拼接并设置属性
                        String content = concatenateContent(workflowEntity);
                        workflowEntity.setContent(content);

                        // 在图上只保留邻近的入口方法的关联
                        workflowEntity.getContainedMethodList().clear();
                        workflowEntity.getContainedMethodList().add(startMethodEntity);
                        workflowEntity = workflowRepo.save(workflowEntity);

                        // 生成功能描述
                         funcGenerateService.genWorkflowFunc(workflowEntity);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in Workflow extraction");
        }
    }

    private JavaMethodEntity getStartMethodEntity(WorkflowEntity workflowEntity) {
        try {
            String cypher = MessageFormat.format("""
                MATCH (n:Workflow)-[:CONTAIN]->(m:JavaMethod)
                    WHERE id(n) = {0}
                    RETURN m
                """, String.format("%d", workflowEntity.getId()));
            Result result = neo4jClient.getQueryRunner().run(cypher);
            if(result.hasNext()) {
                Node node = result.next().get("m").asNode();
                return javaMethodRepo.findById(node.id()).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取工作流的方法调用链，以及涉及的数据实体类
     * 以Controller直接关联的方法为入口
     * @param workflowEntity
     * @param startMethodEntity
     */
    private void getMethodInvokeChain(WorkflowEntity workflowEntity, JavaMethodEntity startMethodEntity, int recursionDepth) {
        // reach max recursive depth, return
        if(recursionDepth > 6) {
            return;
        }
        try {
            workflowEntity.getContainedMethodList().add(startMethodEntity);
            // set workflow id property of method entity
            startMethodEntity.setCid(workflowEntity.getId());
            javaMethodRepo.save(startMethodEntity);

            // data object access relation
            // include only param_type relation for now
            String cypher = MessageFormat.format("""
                    MATCH (n:JavaMethod)-[r:RETURN_TYPE|PARAM_TYPE]->(m:DataObject)
                    WHERE id(n) = {0}
                    RETURN m
                    """, String.format("%d", startMethodEntity.getId()));
            Result result = neo4jClient.getQueryRunner().run(cypher);
            while (result.hasNext()) {
                Node node = result.next().get("m").asNode();
                JavaClassEntity relatedDataObject = javaClassRepo.findById(node.id()).get();
                workflowEntity.getRelatedDataObjectList().add(relatedDataObject);
            }

            // method call relation
            cypher = MessageFormat.format("""
                MATCH (n:JavaMethod)-[:METHOD_CALL]->(m:JavaMethod)
                    WHERE id(n) = {0}
                    RETURN m
                """, String.format("%d", startMethodEntity.getId()));
            result = neo4jClient.getQueryRunner().run(cypher);
            while(result.hasNext()) {
                Node node = result.next().get("m").asNode();
                JavaMethodEntity invokedMethodEntity = javaMethodRepo.findById(node.id()).get();
                getMethodInvokeChain(workflowEntity, invokedMethodEntity, recursionDepth + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in getMethodInvokeChain");
        }
    }

    /**
     * 将工作流涉及的方法和数据对象内容进行拼接作为content属性
     * @param workflowEntity
     * @return
     */
    private String concatenateContent(WorkflowEntity workflowEntity) {
        StringBuilder str = new StringBuilder();
        for(JavaMethodEntity methodEntity : workflowEntity.getContainedMethodList()) {
            str.append(methodEntity.getContent() + "\n");
        }
        for(JavaClassEntity classEntity : workflowEntity.getRelatedDataObjectList()) {
            str.append(classEntity.getContent() + "\n");
        }
        return str.toString();
    }
}
