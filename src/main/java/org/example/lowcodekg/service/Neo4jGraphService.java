package org.example.lowcodekg.service;

import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;

import java.util.List;

public interface Neo4jGraphService {
    Neo4jNode getNodeDetail(long id);

    List<Neo4jRelation> getRelationList(long id);
}
