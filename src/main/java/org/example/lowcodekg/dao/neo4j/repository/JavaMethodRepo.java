package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.JavaMethodEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(collectionResourceRel = "javaMethod", path = "javaMethod")
public interface JavaMethodRepo extends Neo4jRepository<JavaMethodEntity, Long> {
}
