package org.example.lowcodekg.model.dao.neo4j.entity.page;

import lombok.Data;
import org.example.lowcodekg.model.dao.Describable;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("PageTemplate")
@Data
public class PageEntity implements Describable {

    @Id
    @GeneratedValue
    private Long id;

    @Property("vid")
    private Long vid;

    @Property("name")
    private String name;

    @Property("fullName")
    private String fullName;

    @Property("description")
    private String description;

    @Property("content")
    private String content;

    @Property("category")
    private String category;

    @Property("ir")
    private String ir;

    private List<Float> embedding = new ArrayList<>();

    @Relationship(type = "DEPENDENCY", direction = Relationship.Direction.OUTGOING)
    private List<PageEntity> dependedPageList = new ArrayList<>();

    /**
     * 页面绑定的数据对象(Java类)
     */
    @Relationship(type = "BINDING", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> bindingDataObjectList = new ArrayList<>();

    /**
     * 页面包含的组件
     */
    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<ComponentEntity> componentList = new ArrayList<>();
}
