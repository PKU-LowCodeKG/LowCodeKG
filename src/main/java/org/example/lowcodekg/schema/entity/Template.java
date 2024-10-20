package org.example.lowcodekg.schema.entity;

import lombok.Data;
import org.neo4j.graphdb.Label;

/**
 * 低代码模
 */
@Data
public class Template {

    private static final Label label = Label.label("Template");

    private String name;
}