package org.example.lowcodekg.dao.neo4j.entity;

import lombok.Data;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@ComponentScan
@Data
public class GraphData {
    private List<Link> links;

    private List<Node> nodes;
}

class Relation {
    private int relationshipId;
    private String relationship;
    private Integer lineNumber;
    private boolean isSelf;
}

