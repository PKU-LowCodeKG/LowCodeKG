package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
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

    @Property("type")
    private String type;

    @Property("comment")
    private String comment;

    @Property("description")
    private String description;

    @Relationship(type = "FIELD_TYPE", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> typeList = new ArrayList<>();
}
