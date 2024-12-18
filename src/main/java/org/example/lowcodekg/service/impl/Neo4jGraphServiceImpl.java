package org.example.lowcodekg.service.impl;

import org.example.lowcodekg.dao.neo4j.entity.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.JavaMethodEntity;
import org.example.lowcodekg.dto.CodeGenerationResult;
import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.dto.Neo4jRelation;
import org.example.lowcodekg.dto.Neo4jSubGraph;
import org.example.lowcodekg.service.ElasticSearchService;
import org.example.lowcodekg.service.LLMGenerateService;
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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@Service
public class Neo4jGraphServiceImpl implements Neo4jGraphService {

//    @Autowired
//    private Driver neo4jDriver;

    @Autowired
    private Neo4jClient neo4jClient;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private LLMGenerateService llmGenerateService;

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
            System.out.println(propsMap.get("content"));  // debug，查看节点对应的源代码
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

//    @Override
//    public Neo4jSubGraph codeSearch(String query) {
//        String componentConfigCypher = MessageFormat.format("""
//                MATCH p = (c:Component)-[r:CONTAIN]->()
//                WHERE c.name CONTAINS "{0}"
//                RETURN p
//                """, query);
//        QueryRunner runner = neo4jClient.getQueryRunner();
//        Result result = runner.run(componentConfigCypher);
//        Set<Long> addedNodeIds = new HashSet<>();
//
//        Neo4jSubGraph subGraph = new Neo4jSubGraph();
//        while (result.hasNext()) {
//            Record record = result.next();
//            Path path = record.get("p").asPath();
//            for (Node node : path.nodes()) {
//                if (!addedNodeIds.contains(node.id())) {
//                    subGraph.addNeo4jNode(getNodeDetail(node.id()));
//                    addedNodeIds.add(node.id());
//                }
//            }
//            for (Relationship relationship : path.relationships()) {
//                subGraph.addNeo4jRelation(getRelationDetail(relationship));
//            }
//        }
//        subGraph.setCypher(componentConfigCypher);
//        return subGraph;
//    }

    @Override
    public Neo4jSubGraph findAddTags(String query) {
        List<Long> queryResultIdList = new ArrayList<>();
        queryResultIdList.add(6361L);
        queryResultIdList.add(6476L);

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

        String llmAnswer = llmGenerateService.graphPromptToCode(query, subGraph.getNodes());

        subGraph.setGeneratedCode(llmAnswer);

//        String generatedCode = """
//                @OptLog(optType = SAVE_OR_UPDATE)
//                @ApiOperation(value = "保存或更新角色")
//                @PostMapping("/admin/role")
//                public ResultVO<?> saveOrUpdateRole(@RequestBody @Valid RoleVO roleVO) {
//                    roleService.saveOrUpdateRole(roleVO);
//                    return ResultVO.ok();
//                }
//                """;
//
//        subGraph.setGeneratedCode(generatedCode);

        return subGraph;
    }

    @Override
    public Neo4jSubGraph searchRelevantGraph(String query) {
        List<String> relevantNodeVids = elasticSearchService.searchEmbedding(query);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Neo4jSubGraph subGraph = new Neo4jSubGraph();

        Set<Long> addedNodeIds = new HashSet<>();
        Set<Long> addedRelationIds = new HashSet<>();

        int top = Math.min(2, relevantNodeVids.size());  // 找top-3最相关的节点
        int maxExtendNum = 4;  // 目前限定每个查询出的节点最多扩展出4个节点
        for (int i = 0; i < top; i++) {
            String vid = relevantNodeVids.get(i);
            String oneHopCypher = MessageFormat.format("""
                MATCH (n)-[r]->(m)
                WHERE n.vid = {0}
                RETURN n, m, r
                """, vid);
            Result result = runner.run(oneHopCypher);

            int recordNum = 0;
            while (result.hasNext()) {
                recordNum++;
                if (recordNum > maxExtendNum) {
                    break;
                }
                Record record = result.next();
                Node n = record.get("n").asNode();
                Node m = record.get("m").asNode();
                Relationship r = record.get("r").asRelationship();
                if (!addedNodeIds.contains(n.id())) {
                    subGraph.addNeo4jNode(getNodeDetail(n.id()));
                    addedNodeIds.add(n.id());
                }
                if (!addedRelationIds.contains(r.id())) {
                    subGraph.addNeo4jRelation(getRelationDetail(r));
                    addedRelationIds.add(r.id());
                }
                if (!addedNodeIds.contains(m.id())) {
                    subGraph.addNeo4jNode(getNodeDetail(m.id()));
                    addedNodeIds.add(m.id());
                }
            }
        }

//        String llmAnswer = llmGenerateService.graphPromptToCode(query, subGraph.getNodes());
//
//        subGraph.setGeneratedCode(llmAnswer);
        subGraph.setGeneratedCode("");

        return subGraph;
    }

    @Override
    public List<JavaClassEntity> findAllJavaClass() {
        List<JavaClassEntity> javaClassEntityList = new ArrayList<>();
        String javaClassCypher = """
                MATCH (n:JavaClass)
                RETURN n
                """;

        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(javaClassCypher);
        while (result.hasNext()) {
            Node node = result.next().get("n").asNode();
            Map<String, Object> propsMap  = node.asMap();
            JavaClassEntity javaClass = new JavaClassEntity();
            javaClass.setId(node.id());
            javaClass.setName((String) propsMap.get("name"));
            javaClass.setFullName((String) propsMap.get("fullName"));
            javaClass.setProjectName((String) propsMap.get("projectName"));
            javaClass.setComment((String) propsMap.get("comment"));
            javaClass.setContent((String) propsMap.get("content"));
            javaClassEntityList.add(javaClass);
        }
        return javaClassEntityList;
    }

    @Override
    public List<JavaMethodEntity> findAllJavaMethod() {
        List<JavaMethodEntity> javaMethodEntityList = new ArrayList<>();
        String javaMethodCypher = """
                MATCH (n:JavaMethod)
                RETURN n
                """;

        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(javaMethodCypher);
        while (result.hasNext()) {
            Node node = result.next().get("n").asNode();
            Map<String, Object> propsMap  = node.asMap();
            JavaMethodEntity javaMethod = new JavaMethodEntity();
            javaMethod.setId(node.id());
            javaMethod.setName((String) propsMap.get("name"));
            javaMethod.setFullName((String) propsMap.get("fullName"));
            javaMethod.setProjectName((String) propsMap.get("projectName"));
            javaMethod.setComment((String) propsMap.get("comment"));
            javaMethod.setContent((String) propsMap.get("content"));
            javaMethodEntityList.add(javaMethod);
        }
        return javaMethodEntityList;
    }

    @Override
    public CodeGenerationResult codeGeneration(String query,
                                               Neo4jSubGraph oriSubGraph,
                                               List<Long> remainNodeIds) {
        Neo4jSubGraph filteredSubGraph = new Neo4jSubGraph();
        Set<Long> remainIdSet = new HashSet<>(remainNodeIds);

        for (Neo4jNode neo4jNode : oriSubGraph.getNodes()) {
            if (remainIdSet.contains(neo4jNode.getId())) {
                filteredSubGraph.addNeo4jNode(neo4jNode);
            }
        }

        for (Neo4jRelation neo4jRelation : oriSubGraph.getRelationships()) {
            if (remainIdSet.contains(neo4jRelation.getStartNode()) &&
                    remainIdSet.contains(neo4jRelation.getEndNode())) {
                filteredSubGraph.addNeo4jRelation(neo4jRelation);
            }
        }

        // debug：过滤后的子图
        System.out.println(filteredSubGraph.getNodes().size());
        System.out.println(filteredSubGraph.getRelationships());

        String llmAnswer = llmGenerateService.graphPromptToCode(query, filteredSubGraph.getNodes());
        CodeGenerationResult generationResult = new CodeGenerationResult(llmAnswer);
        return generationResult;
    }
}
