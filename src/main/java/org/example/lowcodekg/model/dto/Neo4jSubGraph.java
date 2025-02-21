package org.example.lowcodekg.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Neo4jSubGraph {
    private final List<Neo4jNode> nodes = new ArrayList<>();
    private final List<Neo4jRelation> relationships = new ArrayList<>();
    private String generatedCode = "";

    public void addNeo4jNode(Neo4jNode neo4jNode) {
        this.nodes.add(neo4jNode);
    }

    public void addNeo4jRelation(Neo4jRelation neo4jRelation) {
        this.relationships.add(neo4jRelation);
    }
}
