package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("ConfigItem")
@Data
public class ConfigItem {
    @Id
    private Long id;

    private String name;

    private String defaultValue;

    private String valueClass;

    private String desc;
}
