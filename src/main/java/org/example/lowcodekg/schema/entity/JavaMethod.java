package org.example.lowcodekg.schema.entity;

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
     * 方法参数列表
     */
    private List<Param> params;


}

class Param {

    private String name;

    private String type;

}