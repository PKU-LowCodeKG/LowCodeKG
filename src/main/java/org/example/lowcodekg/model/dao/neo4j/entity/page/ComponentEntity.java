package org.example.lowcodekg.model.dao.neo4j.entity.page;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.Describable;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j 实体节点：组件
 */
@Node("Component")
@Data
@NoArgsConstructor
public class ComponentEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("category")
    private String category;

    @Property("text")
    private String text;

    @Property("content")
    private String content;

    @Property("description")
    private String description;

    /**
     * 组件对组件的依赖
     */
    @Relationship(type = "DEPENDENCY", direction = Relationship.Direction.OUTGOING)
    private List<ComponentEntity> relatedComponentEntities = new ArrayList<>();

    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.OUTGOING)
    private List<ComponentEntity> childComponentList = new ArrayList<>();

    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<ConfigItemEntity> containedConfigItemEntities = new ArrayList<>();


}
