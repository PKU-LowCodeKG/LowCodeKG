package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * 领域实体类（Java 语言）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaClass {

    private String name;

    private String fullName;

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

    public JavaClassEntity storeInNeo4j(JavaClassRepo javaClassRepo) {
        JavaClassEntity classEntity = new JavaClassEntity();
        classEntity.setName(this.name);
        classEntity.setFullName(this.fullName);
        classEntity.setComment(this.comment);
        classEntity.setContent(this.content);
        classEntity.setDescription(this.description);
        javaClassRepo.save(classEntity);
        return classEntity;
    }
}
