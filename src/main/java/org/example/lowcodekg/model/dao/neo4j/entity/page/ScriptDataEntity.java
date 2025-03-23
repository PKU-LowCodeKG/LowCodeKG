package org.example.lowcodekg.model.dao.neo4j.entity.page;

import lombok.Data;
import org.example.lowcodekg.model.dao.Describable;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("ScriptData")
@Data
public class ScriptDataEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("value")
    private String value;

    @Property("description")
    private String description;
}
