package org.example.lowcodekg.model.dto;

import lombok.Data;

@Data
public class Neo4jRelation {
    private final long startNode;
    private final long endNode;
    private final long id;
    private final String type;

    public Neo4jRelation(long startNode, long endNode, long id, String type) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.id = id;
        this.type = type;
    }
}
