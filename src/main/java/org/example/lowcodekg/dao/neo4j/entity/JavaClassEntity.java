package org.example.lowcodekg.dao.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("JavaClass")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JavaClassEntity {

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

    @Relationship(type = "HAVE_METHOD", direction = Relationship.Direction.OUTGOING)
    private List<JavaMethodEntity> methodList = new ArrayList<>();

    @Relationship(type = "HAVE_FIELD", direction = Relationship.Direction.OUTGOING)
    private List<JavaFieldEntity> fieldList = new ArrayList<>();

}
