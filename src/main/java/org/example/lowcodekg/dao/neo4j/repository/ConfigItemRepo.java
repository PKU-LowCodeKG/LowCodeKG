package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.ConfigItem;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "configItem", path = "configItem")
public interface ConfigItemRepo extends Neo4jRepository<ConfigItem, Long> {

    @Query("MATCH (c:Component)-[:CONTAIN]->(ci:ConfigItem) WHERE c.name = $name RETURN ci")
    List<ConfigItem> findConfigItemsByComponentName(String name);

}
