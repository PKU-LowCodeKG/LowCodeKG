package org.example.lowcodekg.model.dao.neo4j.entity.page;

import lombok.Data;
import org.example.lowcodekg.model.dao.Describable;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("ScriptMethod")
@Data
public class ScriptMethodEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("params")
    private List<String> params;

    @Property("content")
    private String content;

    @Property("description")
    private String description;
}
