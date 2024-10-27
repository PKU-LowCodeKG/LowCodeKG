package org.example.lowcodekg.schema.constant;

import lombok.AllArgsConstructor;

/**
 * 组件类别
 */
@AllArgsConstructor
public enum ComponentCategory {

    WORK_FLOW("工作流", "work_flow"),

    UI_COMPONENT("页面组件", "ui_component");

    private String label;
    private String code;

}
