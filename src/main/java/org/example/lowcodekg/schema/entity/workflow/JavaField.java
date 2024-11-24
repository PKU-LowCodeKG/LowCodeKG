package org.example.lowcodekg.schema.entity.workflow;

import lombok.*;
import org.example.lowcodekg.dao.neo4j.entity.JavaFieldEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaFieldRepo;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JavaField {

    private String name;

    private String fullName;

    private String type;

    private String comment;

    private String description;

    private String belongTo;

    private String fullType;

    /**
     * 记录实体间关系
     */
    private List<JavaClass> filedTypeList = new ArrayList<>();

    public JavaField(String name, String fullName, String type, String comment, String belongTo, String fullType) {
        this.name = name;
        this.fullName = fullName;
        this.type = type;
        this.comment = comment;
        this.belongTo = belongTo;
        this.fullType = fullType;
    }

    public JavaFieldEntity storeInNeo4j(JavaFieldRepo javaFieldRepo) {
        JavaFieldEntity entity = new JavaFieldEntity();
        entity.setName(name);
        entity.setFullName(fullName);
        entity.setType(type);
        entity.setComment(comment);
        entity.setDescription(description);
        return javaFieldRepo.save(entity);
    }
}
