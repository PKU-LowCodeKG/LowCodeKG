package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.lowcodekg.dao.neo4j.entity.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;

import java.util.List;

/**
 * 领域实体类（Java 语言）
 */
@Data
@AllArgsConstructor
public class JavaClass {

    private String name;

    private String fullName;

    private String comment;

    private String content;

    private String description;

    private String superClassType;

    private String superInterfaceType;

    private List<JavaMethod> methodList;

    private List<JavaField> fieldList;

    public JavaClass(String name, String fullName, String comment, String content, String superClassType, String superInterfaceType) {
        this.name = name;
        this.fullName = fullName;
        this.comment = comment;
        this.content = content;
        this.superClassType = superClassType;
        this.superInterfaceType = superInterfaceType;
    }


    public void storeInNeo4j(JavaClassRepo javaClassRepo) {
        try {
            JavaClassEntity javaClassEntity = new JavaClassEntity();
        } catch (Exception e) {
            System.err.println("Error in storeInNeo4j: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
