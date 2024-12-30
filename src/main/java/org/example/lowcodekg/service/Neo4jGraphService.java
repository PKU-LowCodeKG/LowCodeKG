package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.dto.CodeGenerationResult;
import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;
import org.example.lowcodekg.dto.Neo4jSubGraph;

import java.util.List;

public interface Neo4jGraphService {
    Neo4jNode getNodeDetail(long id);

    List<Neo4jRelation> getRelationList(long id);

//    Neo4jSubGraph codeSearch(String query);

    Neo4jSubGraph findAddTags(String query);

    Neo4jSubGraph searchRelevantGraph(String query);

    List<JavaClassEntity> findAllJavaClass();

    List<JavaMethodEntity> findAllJavaMethod();

    CodeGenerationResult codeGeneration(String query, Neo4jSubGraph oriSubGraph, List<Long> remainNodeIds);
}
