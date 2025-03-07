package org.example.lowcodekg.service;

import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.model.dto.CodeGenerationResult;
import org.example.lowcodekg.model.dto.Neo4jNode;
import org.example.lowcodekg.model.dto.Neo4jRelation;
import org.example.lowcodekg.model.dto.Neo4jSubGraph;

import java.util.List;

public interface Neo4jGraphService {
    Neo4jNode getNodeDetail(long id);

    List<Neo4jRelation> getRelationList(long id);

//    Neo4jSubGraph codeSearch(String query);

    Neo4jSubGraph searchRelevantGraph(String query);
    Neo4jSubGraph searchRelevantGraphByRules(String query);

    List<JavaClassEntity> findAllJavaClass();

    List<JavaMethodEntity> findAllJavaMethod();

    CodeGenerationResult codeGeneration(String query, Neo4jSubGraph oriSubGraph, List<Long> remainNodeIds);

    Neo4jSubGraph searchFixedGraph(String query);
}
