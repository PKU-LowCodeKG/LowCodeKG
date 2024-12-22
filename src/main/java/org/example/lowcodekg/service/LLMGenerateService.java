package org.example.lowcodekg.service;

import org.example.lowcodekg.dto.Neo4jNode;

import java.util.List;
import java.util.Map;

public interface LLMGenerateService {
    String graphPromptToCode(String query, List<Neo4jNode> nodes);

    List<Map<String, Object>> selectInitialNodes(String query, List<Map<String, Object>> initialNodeProps);

    List<Integer> selectExtendNode(String query, List<Map<String, Object>> extendNodeProps);
}
