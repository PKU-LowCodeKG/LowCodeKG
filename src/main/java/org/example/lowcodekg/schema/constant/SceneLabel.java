package org.example.lowcodekg.schema.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum SceneLabel {

    ECOMMERCE("电商", "ecommerce"),

    FINANCE("金融", "finance"),

    EDUCATION("教育", "education"),

    ENTERTAINMENT("娱乐", "entertainment"),

    GENERAL("通用", "general");

    @Getter
    @Setter
    private String label;
    @Getter
    @Setter
    private String code;

    public static SceneLabel setByCode(String code) {
        return GENERAL;
    }
}
