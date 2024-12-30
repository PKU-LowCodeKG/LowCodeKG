package org.example.lowcodekg.dao.neo4j.entity.page;

import lombok.Data;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.springframework.data.neo4j.core.schema.*;

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

    @Property("description")
    private String description;

    @Property("content")
    private String content;

    @Property("category")
    private String category;

    @Relationship(type = "NESTING", direction = Relationship.Direction.OUTGOING)
    private List<PageEntity> nestingPageList = new ArrayList<>();

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
