package org.example.lowcodekg.schema.entity.bo.java;

import lombok.Data;

/**
 * 领域实体类（Java 语言）
 */
@Data
public class JavaClass {

    private String name;

    private boolean isInterface;

    private String visibility;

    private String modifier;

    private String content;

    private String description;

    private String superClassType;

    private String superInterfaceType;
}
