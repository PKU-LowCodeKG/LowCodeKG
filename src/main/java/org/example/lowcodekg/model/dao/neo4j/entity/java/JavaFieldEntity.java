package org.example.lowcodekg.model.dao.neo4j.entity.java;

import lombok.Data;
import org.example.lowcodekg.model.dao.Describable;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("JavaField")
@Data
public class JavaFieldEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("vid")
    private Long vid;

    @Property("name")
    private String name;

    @Property("fullName")
    private String fullName;

    @Property("projectName")
    private String projectName;

    @Property("type")
    private String type;

    @Property("comment")
    private String comment;

    @Property("description")
    private String description;

    @Relationship(type = "FIELD_TYPE", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> typeList = new ArrayList<>();
}
