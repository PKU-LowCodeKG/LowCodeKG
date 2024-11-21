package org.example.lowcodekg.schema.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.graphdb.Label;

import java.util.List;

/**
 * 低代码模板
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Template {

    private String name;

    private String cnName;

    private String description;

    private String sourceCode;

    private Category category;

    private List<Component> componentList;

}
