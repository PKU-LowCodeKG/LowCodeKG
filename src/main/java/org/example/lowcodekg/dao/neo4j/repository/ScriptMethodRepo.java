package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.page.ScriptMethodEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptMethodRepo extends Neo4jRepository<ScriptMethodEntity, Long> {
}
