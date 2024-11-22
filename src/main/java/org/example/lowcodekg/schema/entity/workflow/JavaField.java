package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JavaField {

    private String name;

    private String type;

    private String visibility;

    private String modifier;

    private String content;

    private String description;
}
