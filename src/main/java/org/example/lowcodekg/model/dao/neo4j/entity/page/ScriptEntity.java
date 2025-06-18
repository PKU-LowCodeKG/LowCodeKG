package org.example.lowcodekg.model.dao.neo4j.entity.page;

import lombok.Data;
import org.example.lowcodekg.model.dao.Describable;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
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

    @Property("imports")
    private String importsComponentList;

    @Relationship(type = "CONTAIN_METHOD", direction = Relationship.Direction.OUTGOING)
    private List<ScriptMethodEntity> methodList = new ArrayList<>();
}
