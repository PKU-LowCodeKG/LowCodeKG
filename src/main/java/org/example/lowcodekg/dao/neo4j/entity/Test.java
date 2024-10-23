package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Data
public class Test {
    @Id
    private Long id;

    private String name;

    // Getters（获取器）和 Setters（设置器）
}