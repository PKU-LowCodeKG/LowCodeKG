package org.example.lowcodekg.service;

import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;
import org.example.lowcodekg.dto.Neo4jSubGraph;

import java.util.List;

public interface Neo4jGraphService {
    Neo4jNode getNodeDetail(long id);

    List<Neo4jRelation> getRelationList(long id);

    Neo4jSubGraph codeSearch(String query);
}
