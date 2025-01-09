package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.page.ConfigItemEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigItemRepo extends Neo4jRepository<ConfigItemEntity, Long> {

    @Query("MATCH (c:Component)-[:CONTAIN]->(ci:ConfigItem) WHERE c.name = $name RETURN ci")
    List<ConfigItemEntity> findConfigItemsByComponentName(String name);

    @Query("MATCH (s:ConfigItem) WHERE id(s)=$sid " +
            "MATCH (e:ScriptMethod) WHERE id(e)=$eid " +
            "CREATE (s)-[:RELATED_TO]->(e)")
    void createRelationOfRelatedMethod(Long sid, Long eid);
}
