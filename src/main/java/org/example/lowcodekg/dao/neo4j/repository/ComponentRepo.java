package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.Component;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "component", path = "component")
public interface ComponentRepo extends Neo4jRepository<Component, Long> {

    @RestResource(path = "name", rel = "name")
    Component findByName(String name);

    @Query("MATCH (c:Component) WHERE c.name CONTAINS $key RETURN c")
    List<Component> findByNameContaining(String key);
}
