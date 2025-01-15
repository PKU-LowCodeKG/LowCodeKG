package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaMethodRepo extends Neo4jRepository<JavaMethodEntity, Long> {

    @Query("MATCH (s:JavaMethod) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:PARAM_TYPE]->(e)")
    void createRelationOfParamType(Long sid, Long eid);

    @Query("MATCH (s:JavaMethod) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:RETURN_TYPE]->(e)")
    void createRelationOfReturnType(Long sid, Long eid);

    @Query("MATCH (s:JavaMethod) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:VARIABLE_TYPE]->(e)")
    void createRelationOfVariableType(Long sid, Long eid);

    @Query("MATCH (s:JavaMethod) WHERE id(s)=$sid " +
            "MATCH (e:JavaMethod) WHERE id(e)=$eid " +
            "CREATE (s)-[:METHOD_CALL]->(e)")
    void createRelationOfMethodCall(Long sid, Long eid);

    @Query("MATCH (s:JavaMethod) WHERE id(s)=$sid " +
            "MATCH (e:JavaField) WHERE id(e)=$eid " +
            "CREATE (s)-[:FIELD_ACCESS]->(e)")
    void createRelationOfFieldAccess(Long sid, Long eid);
}
