package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.JavaFieldEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(collectionResourceRel = "javaMethod", path = "javaMethod")
public interface JavaFieldRepo extends Neo4jRepository<JavaFieldEntity, Long> {

    @Query("MATCH (s:JavaField) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:FIELD_TYPE]->(e)")
    void createRelationOfFieldType(Long sid, Long eid);
}
