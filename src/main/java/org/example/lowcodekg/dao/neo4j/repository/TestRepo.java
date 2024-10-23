package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.Test;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "test", path = "test")
public interface TestRepo extends PagingAndSortingRepository<Test, Long>, CrudRepository<Test, Long>, Neo4jRepository<Test, Long> {
    @RestResource(path = "name", rel = "name")
    public List<Test> findByName(@Param("name") String name);

}
