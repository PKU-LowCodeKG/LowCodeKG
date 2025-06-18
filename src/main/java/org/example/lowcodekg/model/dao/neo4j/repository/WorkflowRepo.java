package org.example.lowcodekg.model.dao.neo4j.repository;

import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepo extends Neo4jRepository<WorkflowEntity, Long> {

    @Query("MATCH (s:Workflow) WHERE id(s)=$sid " +
            "MATCH (e:JavaMethod) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedMethod(Long sid, Long eid);

    @Query("MATCH (s:Workflow) WHERE id(s)=$sid " +
            "MATCH (e:Workflow) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedWorkflow(Long sid, Long eid);

    /**
     * 将包含多个工作流节点的实体节点设置为工作流模块
     */
    @Query("Match (n:Workflow) WHERE id(n)=$id " +
            "SET n:WorkflowModule " +
            "return n")
    void setWorkflowModule(Long id);

    /**
     * 将前端PageTemplate实体通过url匹配建立与Workflow实体的关联
     */
    @Query("MATCH (p:PageTemplate), (w:Workflow) WHERE id(p)=$pid and id(w)=$wid " +
            "CREATE (p)-[:BINDING]->(w)")
    void createRelationBetweenPageAndWorkflow(Long pid, Long wid);
}
