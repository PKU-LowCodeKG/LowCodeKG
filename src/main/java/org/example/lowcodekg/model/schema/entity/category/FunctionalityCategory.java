package org.example.lowcodekg.model.schema.entity.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 组件的功能类别，包含通用的功能类别以及面向特定领域的功能类别
 */
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

    public static List<FunctionalityCategory> setByCode(String code) {
        // TODO:功能分类
        return Collections.singletonList(GENERAL);
    }
}
