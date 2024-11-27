package org.example.lowcodekg.service.impl;

import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;
import org.example.lowcodekg.dto.Neo4jSubGraph;
import org.example.lowcodekg.service.Neo4jGraphService;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class Neo4jGraphServiceImpl implements Neo4jGraphService {

//    @Autowired
//    private Driver neo4jDriver;

    @Autowired
    private Neo4jClient neo4jClient;

    @Override
    public Neo4jNode getNodeDetail(long id) {
        String formattedId = String.format("%d", id);
        String nodeCypher = MessageFormat.format("""
                MATCH (n)
                WHERE id(n) = {0}
                RETURN n
                """, formattedId);
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

    private Neo4jRelation getRelationDetail(Relationship relation) {
        Neo4jRelation neo4jRelation = new Neo4jRelation(
                relation.startNodeId(),
                relation.endNodeId(),
                relation.id(),
                relation.type()
        );
        return neo4jRelation;
    }

    @Override
    public List<Neo4jRelation> getRelationList(long id) {
        String formattedId = String.format("%d", id);
        String relationCypher = MessageFormat.format("""
                MATCH (n)-[r]->()
                WHERE id(n) = {0}
                RETURN r
                """, formattedId);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(relationCypher);

        List<Neo4jRelation> relationList = new ArrayList<>();
        while (result.hasNext()) {
            Record record = result.next();
            Relationship relationship = record.get("r").asRelationship();
            Neo4jRelation neo4jRelation = getRelationDetail(relationship);
            relationList.add(neo4jRelation);
        }
        return relationList;
    }

    @Override
    public Neo4jSubGraph codeSearch(String query) {
        String componentConfigCypher = MessageFormat.format("""
                MATCH p = (c:Component)-[r:CONTAIN]->()
                WHERE c.name CONTAINS "{0}"
                RETURN p
                """, query);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(componentConfigCypher);
        Set<Long> addedNodeIds = new HashSet<>();

        Neo4jSubGraph subGraph = new Neo4jSubGraph();
        while (result.hasNext()) {
            Record record = result.next();
            Path path = record.get("p").asPath();
            for (Node node : path.nodes()) {
                if (!addedNodeIds.contains(node.id())) {
                    subGraph.addNeo4jNode(getNodeDetail(node.id()));
                    addedNodeIds.add(node.id());
                }
            }
            for (Relationship relationship : path.relationships()) {
                subGraph.addNeo4jRelation(getRelationDetail(relationship));
            }
        }
        subGraph.setCypher(componentConfigCypher);
        return subGraph;
    }

    @Override
    public Neo4jSubGraph findAddTags(String query) {
        List<Long> queryResultIdList = new ArrayList<>();
        queryResultIdList.add(1526L);
        queryResultIdList.add(1641L);

        QueryRunner runner = neo4jClient.getQueryRunner();
        Neo4jSubGraph subGraph = new Neo4jSubGraph();
        Set<Long> addedNodeIds = new HashSet<>();
        Set<Long> addedRelationIds = new HashSet<>();
        for (Long queryResultId : queryResultIdList) {
            String formattedId = String.format("%d", queryResultId);
            String oneHopCypher = MessageFormat.format("""
                    MATCH (n)-[r]->(m)
                    WHERE id(n) = {0}
                    RETURN n, m, r
                    """, formattedId);
            Result result = runner.run(oneHopCypher);
            while (result.hasNext()) {
                Record record = result.next();
                Node n = record.get("n").asNode();
                Node m = record.get("m").asNode();
                Relationship r = record.get("r").asRelationship();
                if (!addedNodeIds.contains(n.id())) {
                    subGraph.addNeo4jNode(getNodeDetail(n.id()));
                    addedNodeIds.add(n.id());
                }
                if (!addedNodeIds.contains(m.id())) {
                    subGraph.addNeo4jNode(getNodeDetail(m.id()));
                    addedNodeIds.add(m.id());
                }
                if (!addedRelationIds.contains(r.id())) {
                    subGraph.addNeo4jRelation(getRelationDetail(r));
                    addedRelationIds.add(r.id());
                }
            }
        }

        subGraph.setCypher("cypher");
        return subGraph;
    }
}
