package org.example.lowcodekg.model.schema.entity.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 预先指定框架类型
 */
@AllArgsConstructor
public enum FrameworkType {

    VUE("vue", "vue"),

    REACT("react", "react"),

    ANGULAR("angular", "angular");

    @Getter
    @Setter
    private String label;

    @Getter
    @Setter
    private String code;

}
