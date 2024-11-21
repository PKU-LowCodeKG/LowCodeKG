package org.example.lowcodekg.schema.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum DevMode {

    DATA("数据", "data"),

    WORKFLOW("工作流", "workflow"),

    VIEW("界面", "view");

    @Setter
    @Getter
    private String label;

    @Setter
    @Getter
    private String code;
}
