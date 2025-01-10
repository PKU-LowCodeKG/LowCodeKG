package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.page.ScriptDataEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ScriptDataRepo extends Neo4jRepository<ScriptDataEntity, Long> {
}
