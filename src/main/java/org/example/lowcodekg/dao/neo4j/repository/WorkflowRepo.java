package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.workflow.WorkflowEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface WorkflowRepo extends Neo4jRepository<WorkflowEntity, Long> {

    @Query("MATCH (s:Workflow) WHERE id(s)=$sid " +
            "MATCH (e:JavaMethod) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedMethod(Long sid, Long eid);
}
