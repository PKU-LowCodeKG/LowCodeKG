package org.example.lowcodekg.model.schema.relation;

import org.neo4j.graphdb.RelationshipType;
import org.springframework.data.neo4j.core.schema.Relationship;

public class RelationType {

    /**
     * 包含关系
     */
    public static final RelationshipType CONTAIN = RelationshipType.withName("CONTAIN");

    /**
     * 对 API 的调用关系
     */
    public static final RelationshipType INVOKE = RelationshipType.withName("INVOKE");

    /**
     * 配置项对代码类型实体的关联关系
     */
    public static final RelationshipType BINDING = RelationshipType.withName("BINDING");

    /**
     * 组件之间的依赖关系
     */
    public static final RelationshipType DEPENDENCY = RelationshipType.withName("DEPENDENCY");
}
