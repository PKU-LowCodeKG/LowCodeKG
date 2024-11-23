package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("JavaClass")
@Data
public class JavaClassEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("fullName")
    private String fullName;

    @Property("comment")
    private String comment;

    @Property("content")
    private String content;

    @Property("description")
    private String description;

    @Property("superClassType")
    private String superClassType;

    @Property("superInterfaceType")
    private String superInterfaceType;

    @Relationship(type = "EXTEND", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> superClassList = new ArrayList<>();

    @Relationship(type = "IMPLEMENT", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> superInterfaceList = new ArrayList<>();

    @Relationship(type = "HAVA_METHOD", direction = Relationship.Direction.OUTGOING)
    private List<JavaMethodEntity> methodList = new ArrayList<>();

    @Relationship(type = "HAVA_FIELD", direction = Relationship.Direction.OUTGOING)
    private List<JavaFieldEntity> fieldList = new ArrayList<>();

    public JavaClassEntity() {}

    public JavaClassEntity(String name, String fullName, String comment, String content, String description, String superClassType, String superInterfaceType) {
        this.name = name;
        this.fullName = fullName;
        this.comment =comment;
        this.content = content;
        this.description = description;
        this.superClassType = superClassType;
        this.superInterfaceType = superInterfaceType;
    }
}
