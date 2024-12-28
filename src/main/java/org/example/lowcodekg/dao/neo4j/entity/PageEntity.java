package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.hateoas.server.core.Relation;

import java.util.ArrayList;
import java.util.List;

@Node("PageTemplate")
@Data
public class PageEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("vid")
    private Long vid;

    @Property("name")
    private String name;

    @Property("cnName")
    private String cnName;

    @Property("description")
    private String description;

    @Property("sourceCode")
    private String sourceCode;

    @Property("category")
    private String category;

    @Relationship(type = "NESTING", direction = Relationship.Direction.OUTGOING)
    private List<PageEntity> nestingPageList = new ArrayList<>();

    @Relationship(type = "BINDING", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> bindingDataObjectList = new ArrayList<>();
}
