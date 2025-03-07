package org.example.lowcodekg.model.schema.entity.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 组件所属的特定领域
 */
@AllArgsConstructor
public enum SceneLabel {

    ECOMMERCE("电商", "ecommerce"),

    FINANCE("金融", "finance"),

    EDUCATION("教育", "education"),

    ENTERTAINMENT("娱乐", "entertainment"),

    BLOG("博客", "blog"),

    GENERAL("通用", "general");

    @Getter
    @Setter
    private String label;
    @Getter
    @Setter
    private String code;

    public static List<SceneLabel> setByCode(String code) {
        // TODO:场景标注
        return Collections.singletonList(GENERAL);
    }
}
