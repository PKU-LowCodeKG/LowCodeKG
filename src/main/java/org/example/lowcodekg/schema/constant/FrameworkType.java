package org.example.lowcodekg.schema.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
