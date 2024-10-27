package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
import org.example.lowcodekg.schema.entity.Category;
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

    @Property("description")
    private String description;


    /**
     * 组件对组件的依赖
     */
    @Relationship(type = "DEPENDENCY", direction = Relationship.Direction.OUTGOING)
    private List<ComponentEntity> relatedComponentEntities = new ArrayList<>();
//    private Component relatedComponent;

    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<ConfigItemEntity> containedConfigItemEntities = new ArrayList<>();

    public ComponentEntity(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

}
