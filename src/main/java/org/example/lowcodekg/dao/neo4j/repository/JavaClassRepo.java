package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.workflow.JavaClassEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
//@RepositoryRestResource(collectionResourceRel = "javaClass", path = "javaClass")
public interface JavaClassRepo extends Neo4jRepository<JavaClassEntity, Long> {

    @Query("MATCH (s:JavaClass) WHERE id(s)=$sid " +
            "MATCH (e:JavaField) WHERE id(e)=$eid " +
            "CREATE (s)-[:HAVE_FIELD]->(e)")
    void createRelationOfField(Long sid, Long eid);

    @Query("MATCH (s:JavaClass) WHERE id(s)=$sid " +
            "MATCH (e:JavaMethod) WHERE id(e)=$eid " +
            "CREATE (s)-[:HAVE_METHOD]->(e)")
    void createRelationOfMethod(Long sid, Long eid);

    @Query("MATCH (s:JavaClass) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:EXTEND]->(e)")
    void createRelationOfExtendClass(Long sid, Long eid);

    @Query("MATCH (s:JavaClass) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:IMPLEMENT]->(e)")
    void createRelationOfInterface(Long sid, Long eid);

    /**
     * 数据实体类，添加数据实体标签
     * @param id 节点 id
     */
    @Query("Match (n:JavaClass) WHERE id(n)=$id " +
            "SET n:DataObject " +
            "return n")
    void setDataObjectLabel(Long id);
}
