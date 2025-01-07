package org.example.lowcodekg.dao.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("Workflow")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("content")
    private String content;

    @Property("methodList")
    private String methodList;

    /**
     * 工作流直接关联的method实体
     */
    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<JavaMethodEntity> containedMethodList = new ArrayList<>();
}
