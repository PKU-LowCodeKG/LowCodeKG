package org.example.lowcodekg.model.dao.neo4j.repository;

import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepo extends Neo4jRepository<PageEntity, Long> {

    @Query("MATCH (s:PageTemplate) WHERE id(s)=$sid " +
            "MATCH (e:PageTemplate) WHERE id(e)=$eid " +
            "CREATE (s)-[:DEPENDENCY]->(e)")
    void createRelationOfDependedPage(Long sid, Long eid);

    @Query("MATCH (s:PageTemplate) WHERE id(s)=$sid " +
            "MATCH (e:Component) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedComponent(Long sid, Long eid);

    @Query("MATCH (s:PageTemplate) WHERE id(s)=$sid " +
            "MATCH (e:Script) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedScript(Long sid, Long eid);
}
