package org.example.lowcodekg.schema.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SceneLabel {

    ECOMMERCE("电商", "ecommerce"),

    FINANCE("金融", "finance"),

    EDUCATION("教育", "education"),

    ENTERTAINMENT("娱乐", "entertainment"),

    OTHER("其他", "other");

     private String label;
     private String code;
}
