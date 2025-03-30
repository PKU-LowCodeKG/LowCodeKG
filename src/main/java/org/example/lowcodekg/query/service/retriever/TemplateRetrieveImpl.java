package org.example.lowcodekg.query.service.retriever;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.utils.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.lowcodekg.query.utils.Constants.*;
import static org.example.lowcodekg.query.utils.Prompt.TYPE_OF_RETRIEVED_ENTITY_PROMPT;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:58
 */
@Service
public class TemplateRetrieveImpl implements TemplateRetrieve {

    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private DebugConfig debugConfig;


    @Override
    public Result<List<Node>> queryByTask(String query) {
        List<Node> nodes = new ArrayList<>();
        try {
            // 检索页面实体
            List<Node> pageEntities = queryCategoryEntitiesByTask(query, PAGE_INDEX_NAME);
            nodes.addAll(pageEntities);

            // 检索工作流实体
            List<Node> workflowEntities = queryCategoryEntitiesByTask(query, WORKFLOW_INDEX_NAME);
            nodes.addAll(workflowEntities);

            // 检索数据对象实体
            List<Node> dataObjectEntities = queryCategoryEntitiesByTask(query, DATA_OBJECT_INDEX_NAME);
            nodes.addAll(dataObjectEntities);

            return Result.build(nodes, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in queryEntitiesByTask: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    @Override
    public Result<List<Node>> queryBySubTask(Task task) {
        try {
            List<Node> nodeList = new ArrayList<>();
            List<Document> documents = new ArrayList<>();

            // 根据task任务信息，判断检索的对象类型
            String taskInfo = task.toString();
            if(debugConfig.isDebugMode()) {
                System.out.println("子任务检索信息：" + taskInfo);
            }
            String prompt = TYPE_OF_RETRIEVED_ENTITY_PROMPT.replace("{Task}", taskInfo);
            String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
            if(debugConfig.isDebugMode()) {
                System.out.println("retrieval category: " + answer);
            }
            JSONObject jsonObject = JSON.parseObject(answer);

            // 基于ES向量检索，获取候选列表
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(taskInfo));
            for(String key : jsonObject.keySet()) {
                if("Page".equals(key)) {
                    Boolean isPage = jsonObject.getBoolean(key);
                    if(BooleanUtils.isTrue(isPage)) {
                        documents.addAll(esService.searchByVector(
                                vector, MAX_RESULTS, MIN_SCORE, PAGE_INDEX_NAME
                                ));
                    }
                } else if("Workflow".equals(key)) {
                    Boolean isWorkflow = jsonObject.getBoolean(key);
                    if(BooleanUtils.isTrue(isWorkflow)) {
                        documents.addAll(esService.searchByVector(
                                vector, MAX_RESULTS, MIN_SCORE, WORKFLOW_INDEX_NAME
                                ));
                    }
                } else if("DataObject".equals(key)) {
                    Boolean isDataObject = jsonObject.getBoolean(key);
                    if(BooleanUtils.isTrue(isDataObject)) {
                        documents.addAll(esService.searchByVector(
                                vector, MAX_RESULTS, MIN_SCORE, DATA_OBJECT_INDEX_NAME
                                ));
                    }
                }
            }
            // 将 Document 转换为 Node 并返回 Result
            nodeList = documents.stream()
                    .map(this::convertToNeo4jNode)
                    .collect(Collectors.toList());
            System.out.println(String.format("SubTask {} retrieved results: {}", task.getName(), nodeList.size()));

            return Result.build(nodeList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in queryEntitiesBySubTask: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private List<Node> queryCategoryEntitiesByTask(String query, String indexName) {
        List<Node> pageEntities = new ArrayList<>();
        List<Document> documentsByText = new ArrayList<>();
        List<Document> documentsByVector = new ArrayList<>();
        try {
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(query));
            if(indexName.equals(PAGE_INDEX_NAME)) {
                documentsByText = esService.searchByText(query, 3, 0, PAGE_INDEX_NAME);
                documentsByVector = esService.searchByVector(vector, 3, 0, PAGE_INDEX_NAME);
            } else if(indexName.equals(WORKFLOW_INDEX_NAME)) {
                documentsByText = esService.searchByText(query, 3, 0, WORKFLOW_INDEX_NAME);
                documentsByVector = esService.searchByVector(vector, 3, 0, WORKFLOW_INDEX_NAME);
            } else if(indexName.equals(DATA_OBJECT_INDEX_NAME)) {
                documentsByText = esService.searchByText(query, 3, 0, DATA_OBJECT_INDEX_NAME);
                documentsByVector = esService.searchByVector(vector, 3, 0, DATA_OBJECT_INDEX_NAME);
            }

            List<Document> intersection = documentsByText.stream()
                    .filter(documentsByVector::contains)
                    .collect(Collectors.toList());

            // 将 Document 转换为 Node 并返回 Result
            pageEntities = intersection.stream()
                    .map(this::convertToNeo4jNode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in queryPageEntitiesByTask: " + e.getMessage());
        }
        return pageEntities;
    }

    private Node convertToNeo4jNode(Document document) {
        // 根据 Document 的属性创建 Neo4jNode
        Node node = new Node();
        Long id = Long.valueOf(document.getId());
        String cypher = "MATCH (n) WHERE ID(n) = " + id + " RETURN n";
        QueryRunner runner = neo4jClient.getQueryRunner();
        org.neo4j.driver.Result result = runner.run(cypher);
        if(result.hasNext()) {
            org.neo4j.driver.types.Node n = result.next().get("n").asNode();

            node.setName(n.get("name").asString());
            node.setContent(n.get("content").asString());
            node.setDescription(n.get("description").asString());
        } else {
            return null;
        }
        return node;
    }
}
