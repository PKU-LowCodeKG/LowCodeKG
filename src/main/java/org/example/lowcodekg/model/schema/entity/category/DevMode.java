package org.example.lowcodekg.model.schema.entity.category;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum DevMode {

    DATA("数据建模", "data"),

    WORKFLOW("工作流逻辑", "workflow"),

    VIEW("界面组件", "view");

    @Setter
    @Getter
    private String label;

    @Setter
    @Getter
    private String code;
}
