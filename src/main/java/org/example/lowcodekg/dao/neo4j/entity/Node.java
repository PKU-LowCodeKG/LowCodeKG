package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;

@Data
public class Node {
    private int id;
    private String name;
    private String label;
    private String desc;
    private String classification;
    private String source;
    private String baseCode;
    private Boolean defaultValue;
    private String valueClass;
}
