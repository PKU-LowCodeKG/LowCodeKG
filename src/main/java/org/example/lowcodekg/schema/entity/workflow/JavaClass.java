package org.example.lowcodekg.schema.entity.workflow;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 领域实体类（Java 语言）
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JavaClass {

    /**
     * 向量检索 id
     */
    private Long vid;

    private String name;

    private String fullName;

    private String projectName;

    private String comment;

    private String content;

    private String description;

    private String superClassType;

    private String superInterfaceType;

    /**
     * 记录实体间关系
     */
    private List<JavaClass> superClassList = new ArrayList<>();
    private List<JavaClass> superInterfaceList = new ArrayList<>();
    private List<JavaMethod> containMethodList = new ArrayList<>();
    private List<JavaField> containFieldList = new ArrayList<>();

    public JavaClass(String name, String fullName, String comment, String content, String superClassType, String superInterfaceType) {
        this.name = name;
        this.fullName = fullName;
        this.comment = comment;
        this.content = content;
        this.superClassType = superClassType;
        this.superInterfaceType = superInterfaceType;
    }

    public JavaClassEntity storeInNeo4j(JavaClassRepo javaClassRepo, JSONObject jsonContent) {
        JavaClassEntity classEntity = new JavaClassEntity();
        classEntity.setName(this.name);
        classEntity.setFullName(this.fullName);
        classEntity.setComment(this.comment);
        classEntity.setContent(this.content);
        classEntity.setProjectName(this.projectName);
        if(!Objects.isNull(jsonContent)) {
            classEntity.setVid(jsonContent.getLong("id"));
            classEntity.setDescription(jsonContent.getString("description"));
        }
        return javaClassRepo.save(classEntity);
    }
}
