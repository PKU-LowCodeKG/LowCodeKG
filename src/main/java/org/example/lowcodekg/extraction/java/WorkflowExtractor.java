package org.example.lowcodekg.extraction.java;

import org.example.lowcodekg.dao.neo4j.entity.WorkflowEntity;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

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
                    workflowRepo.save(workflowEntity);
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

    private void getMethodInvokeChain(WorkflowEntity workflowEntity, JavaMethodEntity startMethodEntity) {
        try {
            workflowEntity.getContainedMethodList().add(startMethodEntity);
            // set workflow id property of method entity
            startMethodEntity.setCid(workflowEntity.getId());
            javaMethodRepo.save(startMethodEntity);

            String cypher = MessageFormat.format("""
                MATCH (n:JavaMethod)-[:METHOD_CALL]->(m:JavaMethod)
                    WHERE id(n) = {0}
                    RETURN m
                """, String.format("%d", startMethodEntity.getId()));
            Result result = neo4jClient.getQueryRunner().run(cypher);
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
