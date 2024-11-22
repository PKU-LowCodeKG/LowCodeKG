package org.example.lowcodekg.schema.entity.workflow;

import lombok.Data;

@Data
public class Field {

    private String name;

    private String type;

    private String visibility;

    private String modifier;

    private String content;

    private String description;
}
