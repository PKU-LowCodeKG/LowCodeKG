package org.example.lowcodekg.schema.entity.workflow;

import lombok.Data;

import java.util.List;

/**
 * 领域实体类（Java 语言）
 */
@Data
public class Class {

    private String name;

    private boolean isInterface;

    private String visibility;

    private String modifier;

    private String content;

    private String description;

    private String superClassType;

    private String superInterfaceType;

    private List<Field> fieldList;

    private List<Method> methodList;
}
