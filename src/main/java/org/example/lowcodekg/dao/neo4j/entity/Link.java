package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;

@Data
public class Link {
    private Long source;
    private Long target;
    private Relation relation;
}
