package org.example.lowcodekg.schema.entity.bo.java;

import lombok.Data;

import java.util.List;

@Data
public class JavaMethod {

    private String name;

    private String returnType;

    private String visibility;

    /**
     * 方法修饰符，如static、abstract、final、synchronized、native等
     */
    private String modifier;

    private String content;

    private String description;

    /**
     * 参数列表
     */
    private List<String> params;
}
