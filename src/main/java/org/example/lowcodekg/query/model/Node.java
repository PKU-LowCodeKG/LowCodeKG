package org.example.lowcodekg.query.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;

import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description 检索得到的实体类型
 * @Author Sherloque
 * @Date 2025/3/23 21:13
 */
@Data
@NoArgsConstructor
public class Node {

    private Long id;

    private String name;

    /**
     * 资源全路径名称，用于实验验证标识
     */
    private String fullName;

    private String label;

    private String content;

    private String description;

    private List<IR> irList;

    public Node(PageEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.label = "PageTemplate";
        this.content = entity.getContent();
        this.description = entity.getDescription();
        this.irList = JSONObject.parseArray(entity.getIr(), IR.class);
    }

    public Node(WorkflowEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.label = "Workflow";
        this.content = entity.getContent();
        this.description = entity.getDescription();
        this.irList = JSONObject.parseArray(entity.getIr(), IR.class);
    }

    public Node(JavaClassEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.label = "DataObject";
        this.content = entity.getContent();
        this.description = entity.getDescription();
        this.irList = JSONObject.parseArray(entity.getIr(), IR.class);
    }

    @Override
    public String toString() {
        return "Resource {" +
                "name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", label='" + label + '\'' +
//                ", content='" + content + '\'' +
                ", description='" + description + '\'' +
                ", irList=" + irList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(name, node.name) && Objects.equals(fullName, node.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fullName);
    }
}
