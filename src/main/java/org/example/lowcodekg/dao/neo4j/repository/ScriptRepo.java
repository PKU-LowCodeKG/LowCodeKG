package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.page.ScriptEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepo extends Neo4jRepository<ScriptEntity, Long> {

    @Query("MATCH (s:Script) WHERE id(s)=$sid " +
            "MATCH (e:ScriptMethod) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedMethod(Long sid, Long eid);
}
