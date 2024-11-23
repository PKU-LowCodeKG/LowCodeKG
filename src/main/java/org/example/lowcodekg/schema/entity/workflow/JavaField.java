package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.lowcodekg.dao.neo4j.repository.JavaFieldRepo;

@Data
@AllArgsConstructor
public class JavaField {

    private String name;

    private String fullName;

    private String type;

    private String comment;

    private String description;

    private String belongTo;

    private String fullType;

    public JavaField(String name, String fullName, String type, String comment, String belongTo, String fullType) {
        this.name = name;
        this.fullName = fullName;
        this.type = type;
        this.comment = comment;
        this.belongTo = belongTo;
        this.fullType = fullType;
    }

    public void storeInNeo4j(JavaFieldRepo javaFieldRepo) {

    }
}
