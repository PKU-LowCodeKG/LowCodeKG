package org.example.lowcodekg.dao.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Neo4j 实体节点：组件
 */
@Node("Component")
@Data
public class Component {

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
    private List<Component> relatedComponents = new ArrayList<>();
//    private Component relatedComponent;

    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<ConfigItem> containedConfigItems = new ArrayList<>();

    public Component(String name, ComponentCategory category, SceneLabel sceneLabel, String description) {
        this.name = name;
        this.category = category;
        this.sceneLabel = sceneLabel;
        this.description = description;
    }

}
