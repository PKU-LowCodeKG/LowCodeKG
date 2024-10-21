package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j 实体节点：组件
 */
@Data
@Node("Component")
public class ComponentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("category")
    private ComponentCategory category;

    @Property("sceneLabel")
    private SceneLabel sceneLabel;

    @Property("description")
    private String description;


    /**
     * 组件对组件的依赖
     */
    @Relationship(type = "DEPENDENCY", direction = Relationship.Direction.OUTGOING)
    private List<ComponentEntity> relatedComponents = new ArrayList<>();
//    private Component relatedComponent;

    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<ConfigItemEntity> containedConfigItems = new ArrayList<>();

    public ComponentEntity(String name, ComponentCategory category, SceneLabel sceneLabel, String description) {
        this.name = name;
        this.category = category;
        this.sceneLabel = sceneLabel;
        this.description = description;
    }


}
