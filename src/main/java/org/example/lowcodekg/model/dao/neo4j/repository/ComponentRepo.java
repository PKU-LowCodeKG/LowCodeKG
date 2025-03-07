package org.example.lowcodekg.model.dao.neo4j.repository;

import org.example.lowcodekg.model.dao.neo4j.entity.page.ComponentEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentRepo extends Neo4jRepository<ComponentEntity, Long> {

    @RestResource(path = "name", rel = "name")
    ComponentEntity findByName(String name);

    @Query("MATCH (c:Component) WHERE c.name CONTAINS $key RETURN c")
    List<ComponentEntity> findByNameContaining(String key);

    @Query("MATCH (s:Component) WHERE id(s)=$sid " +
            "MATCH (e:Component) WHERE id(e)=$eid " +
            "CREATE (s)-[:PARENT_OF]->(e)")
    void createRelationOfChildComponent(Long sid, Long eid);

    @Query("MATCH (s:Component) WHERE id(s)=$sid " +
            "MATCH (e:ConfigItem) WHERE id(e)=$eid " +
            "CREATE (s)-[:CONTAIN]->(e)")
    void createRelationOfContainedConfigItem(Long sid, Long eid);

    /**
     * 组件定义
     * @param id 节点 id
     */
    @Query("Match (n:Component) WHERE id(n)=$id " +
            "SET n:ComponentDef " +
            "return n")
    void setComponentDef(Long id);

    /**
     * 组件实例
     */
    @Query("Match (n:Component) WHERE id(n)=$id " +
            "SET n:ComponentExample " +
            "return n")
    void setComponentExample(Long id);
}
