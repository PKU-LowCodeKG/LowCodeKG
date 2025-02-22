package org.example.lowcodekg.service.impl;

import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
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
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

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

    // 记录METHOD_CALL的扩展次数
    private int methodCallExtendCount = 0;
    // 记录JavaClass字段的扩展次数
    private int classFieldExtendCount = 0;

    private List<Map<String, Object>> fetchInitialNodeProperties(QueryRunner runner, List<String> relevantNodeVids) {
        List<Map<String, Object>> initialNodeProps = new ArrayList<>();
        int top = Math.min(5, relevantNodeVids.size());
        for (int i = 0; i < top; i++) {
            String initialNodeVid = relevantNodeVids.get(i);
            String nodeVidCypher = MessageFormat.format("""
            MATCH (n)
            WHERE n.vid = {0}
            RETURN n
            """, initialNodeVid);
            Result result = runner.run(nodeVidCypher);
            if (result.hasNext()) {
                Node node = result.next().get("n").asNode();
                Map<String, Object> propsMap = node.asMap();
                Map<String, Object> fullPropsMap = new HashMap<>(propsMap);
                fullPropsMap.put("id", node.id());
                fullPropsMap.put("label", node.labels().iterator().next());
                initialNodeProps.add(fullPropsMap);
            }
        }
        return initialNodeProps;
    }

    private void addInitialNodesToGraph(List<Map<String, Object>> realInitialNodeProps,
                                 Set<Long> addedNodeIds,
                                 Neo4jSubGraph subGraph) {
        for (Map<String, Object> nodeProps : realInitialNodeProps) {
            Long nodeId = (Long) nodeProps.get("id");
            addedNodeIds.add(nodeId);
            subGraph.addNeo4jNode(getNodeDetail(nodeId));
        }
    }

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
//            System.out.println(propsMap.get("content"));  // debug，查看节点对应的源代码
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
//    public Neo4jSubGraph searchRelevantGraph(String query) {
//        List<String> relevantNodeVids = elasticSearchService.searchEmbedding(query);
//        QueryRunner runner = neo4jClient.getQueryRunner();
//        Neo4jSubGraph subGraph = new Neo4jSubGraph();
//
//        Set<Long> addedNodeIds = new HashSet<>();
//        Set<Long> addedRelationIds = new HashSet<>();
//
//        int top = Math.min(2, relevantNodeVids.size());  // 找top-3最相关的节点
//        int maxExtendNum = 4;  // 目前限定每个查询出的节点最多扩展出4个节点
//        for (int i = 0; i < top; i++) {
//            String vid = relevantNodeVids.get(i);
//            String oneHopCypher = MessageFormat.format("""
//                MATCH (n)-[r]->(m)
//                WHERE n.vid = {0}
//                RETURN n, m, r
//                """, vid);
//            Result result = runner.run(oneHopCypher);
//
//            int recordNum = 0;
//            while (result.hasNext()) {
//                recordNum++;
//                if (recordNum > maxExtendNum) {
//                    break;
//                }
//                Record record = result.next();
//                Node n = record.get("n").asNode();
//                Node m = record.get("m").asNode();
//                Relationship r = record.get("r").asRelationship();
//                if (!addedNodeIds.contains(n.id())) {
//                    subGraph.addNeo4jNode(getNodeDetail(n.id()));
//                    addedNodeIds.add(n.id());
//                }
//                if (!addedRelationIds.contains(r.id())) {
//                    subGraph.addNeo4jRelation(getRelationDetail(r));
//                    addedRelationIds.add(r.id());
//                }
//                if (!addedNodeIds.contains(m.id())) {
//                    subGraph.addNeo4jNode(getNodeDetail(m.id()));
//                    addedNodeIds.add(m.id());
//                }
//            }
//        }
//
//        subGraph.setGeneratedCode("");
//
//        return subGraph;
//    }
//

    // 大模型从初始节点筛选种子节点，扩展出种子节点关联的所有节点，再让大模型从扩展出来的节点中进行筛选，最后得到子图
    @Override
    public Neo4jSubGraph searchRelevantGraph(String query) {
        List<String> relevantNodeVids = elasticSearchService.searchEmbedding(query);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Neo4jSubGraph subGraph = new Neo4jSubGraph();

        Set<Long> addedNodeIds = new HashSet<>();
        Set<Long> addedRelationIds = new HashSet<>();

        // 根据es搜索结果的vid属性，从neo4j中获得相应节点的信息
        List<Map<String, Object>> initialNodeProps = fetchInitialNodeProperties(runner, relevantNodeVids);

        // 对于最初根据语义相似度选出的节点，让LLM判断哪几个是真正与功能相关的,把相关的节点加入子图
        List<Map<String, Object> > realInitialNodeProps =
                llmGenerateService.selectInitialNodes(query, initialNodeProps);
        addInitialNodesToGraph(realInitialNodeProps, addedNodeIds, subGraph);


        // 从每个初始节点出发，扩展1-hop关系。关系数量可能较多，让LLM判断扩展哪些节点
        List<Relationship> extendRelationships = new ArrayList<>();
        List<Map<String, Object> > extendNodeProps = new ArrayList<>();
        for (Map<String, Object> nodeProps : realInitialNodeProps) {
            Long vid = (Long) nodeProps.get("vid");
            String vidStr = String.valueOf(vid);
            String oneHopCypher = MessageFormat.format("""
                MATCH (n)-[r]-(m)
                WHERE n.vid = {0}
                RETURN n, m, r
                """, vidStr);
            Result result = runner.run(oneHopCypher);
            while (result.hasNext()) {
                Record record = result.next();
                Node m = record.get("m").asNode();
                Relationship r = record.get("r").asRelationship();

                // 如果扩展出的节点本来就是最初搜出来的节点，就不用让LLM判断了，直接放到搜索结果里
                if (addedNodeIds.contains(m.id())) {
                    addedRelationIds.add(r.id());
                    subGraph.addNeo4jRelation(getRelationDetail(r));
                }

                Map<String, Object> propsMap = m.asMap();
                Map<String, Object> fullPropsMap = new HashMap<>(propsMap);
                fullPropsMap.put("id", m.id());
                fullPropsMap.put("label", m.labels());
                extendRelationships.add(r);
                extendNodeProps.add(fullPropsMap);

            }
        }



        // 获得1-hop关系和节点后让LLM进行筛选
        List<Integer> realExtendIdx =
                llmGenerateService.selectExtendNode(query, extendNodeProps);
        // 将筛选出来的关系和节点加入到待输出的子图
        for (int idx : realExtendIdx) {
            Map<String, Object> mNodeProps = extendNodeProps.get(idx);
            Relationship mRelation = extendRelationships.get(idx);
            Long mId = (Long) mNodeProps.get("id");
            if (!addedNodeIds.contains(mId)) {
                subGraph.addNeo4jNode(getNodeDetail(mId));
                addedNodeIds.add(mId);
            }
            if (!addedRelationIds.contains(mRelation.id())) {
                subGraph.addNeo4jRelation(getRelationDetail(mRelation));
                addedRelationIds.add(mRelation.id());
            }

        }

        subGraph.setGeneratedCode("");
        return subGraph;
    }

    @Override
    public Neo4jSubGraph searchRelevantGraphByRules(String query) {
        List<String> relevantNodeVids = elasticSearchService.searchEmbedding(query);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Neo4jSubGraph subGraph = new Neo4jSubGraph();

        Set<Long> addNodesIds = new HashSet<>();
        Set<Long> addRelationIds = new HashSet<>();
        // 根据es搜索结果的vid属性，从neo4j中获得相应节点信息
        List<Map<String, Object>> initialNodeProps = fetchInitialNodeProperties(runner, relevantNodeVids);

        // 对于最初根据语义相似度选出的节点，让LLM判断哪几个是真正与功能相关的,把相关的节点加入子图
        List<Map<String, Object>> realInitialNodeProps =
                llmGenerateService.selectInitialNodes(query, initialNodeProps);
        addInitialNodesToGraph(realInitialNodeProps, addNodesIds, subGraph);

        // 处理初始的JavaMethod节点
        List<Neo4jNode> realInitialNodes = new ArrayList<>(subGraph.getNodes());
        for(Neo4jNode node : realInitialNodes){
            if("JavaMethod".equals(node.getLabel())){
                expandJavaMethodNode(node, subGraph, addNodesIds, addRelationIds, runner);
            }
        }

        // 处理扩展出的节点
        List<Neo4jNode> nodesToProcess = new ArrayList<>(subGraph.getNodes());
        for(Neo4jNode node : nodesToProcess){
            if("JavaMethod".equals(node.getLabel())) {
                // 对于METHOD_CALL扩展出的节点，继续扩展
                if (methodCallExtendCount < 3) {
                    expandJavaMethodNode(node, subGraph, addNodesIds, addRelationIds, runner);
                }
            }
            else if("JavaClass".equals(node.getLabel())){
                if(classFieldExtendCount < 2){
                    expandJavaClassFields(node, subGraph, addNodesIds, addRelationIds, runner);
                }
            }
        }
        subGraph.setGeneratedCode("");
        return subGraph;
    }

    private boolean isFrequentMethod(QueryRunner runner, Node m) {
        Long id = m.id();
        String idStr = String.valueOf(id);
        String methodCalledCountCypher = MessageFormat.format("""
                MATCH (n)-[r:METHOD_CALL]->(m:JavaMethod)
                WHERE id(m) = {0}
                RETURN COUNT(r) as num
                """, idStr);
        Result result = runner.run(methodCalledCountCypher);
        if (!result.hasNext()) {
            return false;
        }
        int num = result.next().get("num").asInt();
        return num > 10;
    }

    private boolean isFrequentClass(QueryRunner runner, Node m) {
        Long id = m.id();
        String idStr = String.valueOf(id);
        String returnTypeCountCypher = MessageFormat.format("""
                    MATCH (n:JavaMethod)-[r:RETURN_TYPE]->(m:JavaClass)
                    WHERE id(m) = {0}
                    RETURN COUNT(r) as num
                """, idStr);
        Result result = runner.run(returnTypeCountCypher);
        if (!result.hasNext()) {
            return false;
        }
        int num = result.next().get("num").asInt();
        return num > 10;
    }

    private void expandJavaMethodNode(Neo4jNode node, Neo4jSubGraph subGraph,
                                      Set<Long> addNodesIds, Set<Long> addRelationIds,
                                      QueryRunner runner){
        Long id = node.getId();
        String idStr = String.valueOf(id);
        // 1.1 处理METHOD_CALL出边
        String outCallCypher = MessageFormat.format("""
                MATCH (n:JavaMethod)-[r:METHOD_CALL]->(m:JavaMethod)
                WHERE id(n) = {0}
                RETURN m, r
                """, idStr);
        Result result = runner.run(outCallCypher);
        while(result.hasNext()){
            Record record = result.next();
            if (isFrequentMethod(runner, record.get("m").asNode())) {
                continue;
            }
            addNodeAndRelation(record.get("m").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            methodCallExtendCount++;
        }
        // 1.2 处理METHOD_CALL入边
        String inCallCypher = MessageFormat.format("""
                MATCH (m:JavaMethod)-[r:METHOD_CALL]->(n:JavaMethod)
                WHERE id(n) = {0}
                RETURN m, r
                """, idStr);
        result = runner.run(inCallCypher);
        while(result.hasNext()){
            Record record = result.next();
            addNodeAndRelation(record.get("m").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
        }
        // 1.3 处理参数类型
        String paramTypeCypher = MessageFormat.format("""
                MATCH (n:JavaMethod)-[r:PARAM_TYPE]->(m:JavaClass)
                WHERE id(n) = {0}
                RETURN m, r
                """, idStr);
        result = runner.run(paramTypeCypher);
        while(result.hasNext()){
            Record record = result.next();
            addNodeAndRelation(record.get("m").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
        }
        // 1.4 处理返回类型
        String returnTypeCypher = MessageFormat.format("""
                MATCH (n:JavaMethod)-[r:RETURN_TYPE]->(m:JavaClass)
                WHERE id(n) = {0}
                RETURN m, r
                """, idStr);
        result = runner.run(returnTypeCypher);
        while(result.hasNext()){
            Record record = result.next();
            if (isFrequentClass(runner, record.get("m").asNode())) {
                continue;
            }
            addNodeAndRelation(record.get("m").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
        }
        // 1.5 处理所属类
        // 1.5.1 处理接口实现
        String implCypher = MessageFormat.format("""
                MATCH (n1:JavaMethod)<-[r1:HAVE_METHOD]-(n2:JavaClass)-[r2:IMPLEMENT]->(m:JavaClass)-[r:HAVE_METHOD]->(n:JavaMethod)
                WHERE id(n) = {0}
                AND n.name = n1.name
                OPTIONAL MATCH (n1)-[r3:METHOD_CALL]->(n3:JavaMethod)
                RETURN n1, n2, m, n3, r1, r2, r, r3
                """, idStr);
        result = runner.run(implCypher);
        while(result.hasNext()){
            Record record = result.next();
            addNodeAndRelation(record.get("n1").asNode(), record.get("r1").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            addNodeAndRelation(record.get("n2").asNode(), record.get("r2").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            addNodeAndRelation(record.get("m").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            if(record.get("n3") != null){
                addNodeAndRelation(record.get("n3").asNode(), record.get("r3").asRelationship(),
                        subGraph, addNodesIds, addRelationIds);
            }
        }
        // 1.5.2 处理接口定义
        String extendCypher = MessageFormat.format("""
                MATCH (n1:JavaMethod)<-[r1:HAVE_METHOD]-(n2:JavaClass)<-[r2:IMPLEMENT]-(m:JavaClass)-[r:HAVE_METHOD]->(n:JavaMethod)
                WHERE id(n) = {0}
                AND n.name = n1.name
                OPTIONAL MATCH (n3:JavaMethod)-[r3:METHOD_CALL]->(n1)
                RETURN n1, n2,m, n3, r1, r2, r, r3
                """, idStr);
        result = runner.run(extendCypher);
        while(result.hasNext()){
            Record record = result.next();
            addNodeAndRelation(record.get("n1").asNode(), record.get("r1").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            addNodeAndRelation(record.get("n2").asNode(), record.get("r2").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            addNodeAndRelation(record.get("m").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
            if(record.get("n3") != null){
                addNodeAndRelation(record.get("n3").asNode(), record.get("r3").asRelationship(),
                        subGraph, addNodesIds, addRelationIds);
            }
        }
    }
    private void expandJavaClassFields(Neo4jNode node, Neo4jSubGraph subGraph,
                                      Set<Long> addNodesIds, Set<Long> addRelationIds,
                                      QueryRunner runner){
        Long id = node.getId();
        String idStr = String.valueOf(id);
        // 2.3 处理类字段
        String fieldsCypher = MessageFormat.format("""
                MATCH (c:JavaClass)-[r:HAVE_FIELD]->(f:JavaField)
                WHERE id(c) = {0}
                RETURN f, r
                """, idStr);
        // 朴素方法 将所有的Field都加入
        Result result = runner.run(fieldsCypher);

        while(result.hasNext()){
            Record record = result.next();
            addNodeAndRelation(record.get("f").asNode(), record.get("r").asRelationship(),
                    subGraph, addNodesIds, addRelationIds);
        }
        // 使用LLM进行筛选 TODO
//        Result result = runner.run(fieldsCypher);
//        List<Map<String, Object>> fieldsProps = new ArrayList<>();
//        List<Record> records = new ArrayList<>();
//
//        while(result.hasNext()){
//            Record record = result.next();
//            Map<String, Object> fieldProps = new HashMap<>();
//            fieldProps.put("name", record.get("name").asString());
//            fieldProps.put("type", record.get("type").asString());
//            fieldsProps.add(fieldProps);
//            records.add(record);
//        }
//
//        List<Integer> relevantFieldIdx = llmGenerateService.selectRevelantFields(fieldsProps);
//
//        for(Integer idx : relevantFieldIdx){
//            Record record = records.get(idx);
//            addNodeAndRelation(record.get("f").asNode(), record.get("r").asRelationship(),
//                    subGraph, addNodesIds, addRelationIds);
//        }
        classFieldExtendCount++;
    }
    private void addNodeAndRelation(Node node, Relationship relation, Neo4jSubGraph subGraph, Set<Long> addNodesIds, Set<Long> addRelationIds){
        if(!addNodesIds.contains(node.id())){
            subGraph.addNeo4jNode(getNodeDetail(node.id()));
            addNodesIds.add(node.id());
        }
        if(!addRelationIds.contains(relation.id())){
            subGraph.addNeo4jRelation(getRelationDetail(relation));
            addRelationIds.add(relation.id());
        }
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




    public Neo4jRelation getRelationDetailById(long id) {
        String formattedId = String.format("%d", id);
        String nodeCypher = MessageFormat.format("""
                MATCH (n)-[r]->(m)
                WHERE id(r) = {0}
                RETURN r
                """, formattedId);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(nodeCypher);
        if (result.hasNext()) {
            Relationship relation = result.next().get("r").asRelationship();
            Neo4jRelation neo4jRelation = new Neo4jRelation(
                    relation.startNodeId(),
                    relation.endNodeId(),
                    relation.id(),
                    relation.type()
            );
            return neo4jRelation;
        } else {
            return null;
        }
    }

    @Override
    public Neo4jSubGraph searchFixedGraph(String query) {
        Neo4jSubGraph subGraph = new Neo4jSubGraph();
//        List<Integer> nodeIdList = List.of(1723, 7886);

        List<Integer> nodeIdList = List.of(
                1723,
                494,
                9153, 9280, 998, 437,
                9109, 9167,
                2738, 2609, 2958, 2979, 2328,
                2717, 3203, 2800, 2203, 2598
        );
        List<Integer> relationIdList = List.of(
                6514,
                1069, 1044, 2094, 3813,
                5025,
                6516, 3939,
                762, 761, 764, 765, 758,
                1178, 1186, 1181, 1174, 1177
        );

        for (Integer nodeId : nodeIdList) {
            subGraph.addNeo4jNode(getNodeDetail(nodeId));
        }

        for (Integer relationId : relationIdList) {
            subGraph.addNeo4jRelation(getRelationDetailById(relationId));
        }

        subGraph.setGeneratedCode("");
        return subGraph;
    }
}
