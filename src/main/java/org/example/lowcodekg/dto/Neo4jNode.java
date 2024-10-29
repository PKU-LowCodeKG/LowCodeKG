package org.example.lowcodekg.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Neo4jNode {

    private final long id;
    private final String label;
    private final Map<String, Object> properties = new HashMap<>();

    public Neo4jNode(long id, String label, Map<String, Object> properties) {
        this.id = id;
        this.label = label;
        this.properties.putAll(properties);
    }

    public Neo4jNode(long id, String label) {
        this.id = id;
        this.label = label;
    }

}
