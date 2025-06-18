package org.example.lowcodekg.model.dao.neo4j.entity.page;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.Describable;
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

    @Property("name")
    private String name;

    @Property("type")
    private String type;

    @Property("value")
    private String value;

    @Property("description")
    private String description;

}
