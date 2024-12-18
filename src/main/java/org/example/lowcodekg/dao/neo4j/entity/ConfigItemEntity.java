package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("ConfigItem")
@Data
@NoArgsConstructor
public class ConfigItemEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Property("code")
    private String code;

    @Property("type")
    private String type;

    @Property("defaultValue")
    private String defaultValue;

    @Property("description")
    private String description;


    public ConfigItemEntity(String code, String type, String defaultValue, String description) {
        this.code = code;
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
    }
}
