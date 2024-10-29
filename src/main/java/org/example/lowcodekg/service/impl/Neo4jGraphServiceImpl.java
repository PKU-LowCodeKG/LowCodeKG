package org.example.lowcodekg.service.impl;

import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;
import org.example.lowcodekg.service.Neo4jGraphService;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class Neo4jGraphServiceImpl implements Neo4jGraphService {

//    @Autowired
//    private Driver neo4jDriver;

    @Autowired
    private Neo4jClient neo4jClient;

    @Override
    public Neo4jNode getNodeDetail(long id) {
        String nodeCypher = MessageFormat.format("""
                MATCH (n)
                WHERE id(n) = {0}
                RETURN n
                """, id);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(nodeCypher);
        if (result.hasNext()) {
            Node node = result.next().get("n").asNode();
            Neo4jNode neo4jNode = new Neo4jNode(node.id(), node.labels().iterator().next());
            Map<String, Object> propsMap  = node.asMap();
            Set<String> propsKeys = propsMap.keySet();
            for (String key : propsKeys) {
                neo4jNode.getProperties().put(key, propsMap.get(key));
            }
            neo4jNode.getProperties().put("id", neo4jNode.getId());
            neo4jNode.getProperties().put("label", neo4jNode.getLabel());
            return neo4jNode;
        } else {
            return null;
        }
    }

    @Override
    public List<Neo4jRelation> getRelationList(long id) {
        String relationCypher = MessageFormat.format("""
                MATCH (n)-[r]->()
                WHERE id(n) = {0}
                RETURN r
                """, id);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(relationCypher);

        List<Neo4jRelation> relationList = new ArrayList<>();
        while (result.hasNext()) {
            Record record = result.next();
            Relationship relationship = record.get("r").asRelationship();
            Neo4jRelation neo4jRelation = new Neo4jRelation(
                    relationship.startNodeId(),
                    relationship.endNodeId(),
                    relationship.id(),
                    relationship.type()
            );
            relationList.add(neo4jRelation);
        }
        return relationList;
    }
}
