package org.example.lowcodekg.service;

import org.example.lowcodekg.dto.Neo4jNode;

public interface Neo4jGraphService {
    Neo4jNode getNodeDetail(long id);
}
