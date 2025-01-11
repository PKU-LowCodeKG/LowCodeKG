package org.example.lowcodekg.schema.entity.workflow;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaFieldEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaFieldRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JavaField {

    private Long vid;

    private String name;

    private String fullName;

    private String projectName;

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

    public JavaFieldEntity storeInNeo4j(JavaFieldRepo javaFieldRepo, JSONObject jsonContent) {
        JavaFieldEntity fieldEntity = new JavaFieldEntity();
        fieldEntity.setName(name);
        fieldEntity.setFullName(fullName);
        fieldEntity.setProjectName(projectName);
        fieldEntity.setType(type);
        fieldEntity.setComment(comment);
        if(!Objects.isNull(jsonContent)) {
            fieldEntity.setVid(Long.valueOf(jsonContent.getLong("id")));
            fieldEntity.setDescription(jsonContent.getString("description"));
        }
        return javaFieldRepo.save(fieldEntity);
    }
}
