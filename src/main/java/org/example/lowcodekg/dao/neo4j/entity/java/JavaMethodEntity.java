package org.example.lowcodekg.dao.neo4j.entity.java;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("JavaMethod")
@Data
public class JavaMethodEntity {

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

    @Property("returnType")
    private String returnType;

    @Property("content")
    private String content;

    @Property("comment")
    private String comment;

    @Property("description")
    private String description;

    @Property("params")
    private String params;

    @Relationship(type = "PARAM_TYPE", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> paramTypeList = new ArrayList<>();

    @Relationship(type = "RETURN_TYPE", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> returnTypeList = new ArrayList<>();

    @Relationship(type = "VARIABLE_TYPE", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> variableTypeList = new ArrayList<>();

    @Relationship(type = "METHOD_CALL", direction = Relationship.Direction.OUTGOING)
    private List<JavaMethodEntity> methodCallList = new ArrayList<>();

    @Relationship(type = "FIELD_ACCESS", direction = Relationship.Direction.OUTGOING)
    private List<JavaFieldEntity> fieldAccessList = new ArrayList<>();

}
