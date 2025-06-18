package org.example.lowcodekg.model.dao.neo4j.entity.java;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.Describable;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node("Workflow")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowEntity implements Describable {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 多项目工作流实体聚类后生成的工作流实体的id
     */
    @Property("cluster_id")
    private Long cid;

    @Property("name")
    private String name;

    @Property("fullName")
    private String fullName;

    @Property("description")
    private String description;

    @Property("content")
    private String content;

    @Property
    private String mappingUrl;

    @Property("methodList")
    private String methodList;

    @Property
    private String ir;

    private List<Float> embedding = new ArrayList<>();

    /**
     * 工作流关联的method实体
     */
    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<JavaMethodEntity> containedMethodList = new ArrayList<>();

    @Relationship(type = "CONTAIN", direction = Relationship.Direction.OUTGOING)
    private List<JavaClassEntity> relatedDataObjectList = new ArrayList<>();
}
