package org.example.lowcodekg.dao.neo4j.entity;

import dev.langchain4j.agent.tool.P;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("Script")
@Data
public class ScriptEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("content")
    private String content;

    @Property("dataList")
    private String dataList;

    @Property("methodList")
    private List<String> methodList;

    @Property("importsComponentList")
    private List<String> importsComponentList;
}
