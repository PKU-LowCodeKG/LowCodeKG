package org.example.lowcodekg.schema.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum FunctionalityCategory {

    GENERAL("通用", "general"),

    LAYOUT("布局", "layout"),

    NAVIGATION("导航", "navigation"),

    FORM("表单", "form"),

    DATA_INPUT("数据录入", "data_input"),

    DATA_DISPLAY("数据展示", "data_display"),

    FEEDBACK("反馈", "feedback");

    @Getter
    @Setter
    private String label;
    @Getter
    @Setter
    private String code;

    public static FunctionalityCategory setByCode(String code) {
        return GENERAL;
    }
}
