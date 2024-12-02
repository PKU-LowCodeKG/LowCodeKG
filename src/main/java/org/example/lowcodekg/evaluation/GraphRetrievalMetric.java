package org.example.lowcodekg.evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.lowcodekg.dto.Neo4jSubGraph;

/**
 * 评估子图检索结果的指标
 */
@NoArgsConstructor
@AllArgsConstructor
public class GraphRetrievalMetric {

    @Getter
    @Setter
    private Neo4jSubGraph retrievalGraph;
    @Getter
    @Setter
    private Neo4jSubGraph groundTruthGraph;



}
