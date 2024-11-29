package org.example.lowcodekg.service;

import org.example.lowcodekg.dto.Neo4jNode;

import java.util.List;

public interface LLMGenerateService {
    String graphPromptToCode(String query, List<Neo4jNode> nodes);
}
