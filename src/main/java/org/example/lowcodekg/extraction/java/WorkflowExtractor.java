package org.example.lowcodekg.extraction.java;

import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;

import java.text.MessageFormat;
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
            // iterate all the initial workflow entities
            while(result.hasNext()) {
                Node node = result.next().get("n").asNode();
                Optional<WorkflowEntity> optional = workflowRepo.findById(node.id());
                optional.ifPresent(workflowEntity -> {
                    // get start method entity
                    JavaMethodEntity startMethodEntity = getStartMethodEntity(workflowEntity);
                    // get method invoke chain(dfs)
                    getMethodInvokeChain(workflowEntity, startMethodEntity);
                    // concatenate method content
                    String content = concatenateMethodContent(workflowEntity);
                    // set property
                    workflowEntity.setContent(content);
                    // remove redundant relations
                    workflowEntity.getContainedMethodList().clear();
                    workflowEntity = workflowRepo.save(workflowEntity);

                    // generate description
                    funcGenerateService.genWorkflowFunc(workflowEntity);
                });
            }
            // generate workflow module tree
//            functionalityGenService.genWorkflowModule();

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
    private void getMethodInvokeChain(WorkflowEntity workflowEntity, JavaMethodEntity startMethodEntity) {
        try {
            workflowEntity.getContainedMethodList().add(startMethodEntity);
            // set workflow id property of method entity
            startMethodEntity.setCid(workflowEntity.getId());
            javaMethodRepo.save(startMethodEntity);

            // data object access relation
            String cypher = MessageFormat.format("""
                    MATCH (n:JavaMethod)-[:PARAM_TYPE]->(m:DataObject)
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
                getMethodInvokeChain(workflowEntity, invokedMethodEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in getMethodInvokeChain");
        }
    }

    private String concatenateMethodContent(WorkflowEntity workflowEntity) {
        StringBuilder str = new StringBuilder();
        for(JavaMethodEntity methodEntity : workflowEntity.getContainedMethodList()) {
            str.append(methodEntity.getContent() + "\n");
        }
        return str.toString();
    }
}
