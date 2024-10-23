package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Neo4j 实体节点：组件
 */
@Node("Component")
@Data
public class Component {

    @Id
    private Long id;

    @Property("name")
    private String name;

    /**
     * 组件对组件的依赖
     */
    @Relationship(type = "Dependency")
    private Component relatedComponent;

    private String desc;

    private String classification;

    private String source;

    private String baseCode;

    @Relationship(type = "relateConfig")
    private Set<ConfigItem> relateConfigItems = new HashSet<>();

}
