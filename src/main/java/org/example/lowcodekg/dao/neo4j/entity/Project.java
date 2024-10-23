package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@EntityScan
public class Project {

    private String name;

    private String description;

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
