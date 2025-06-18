package org.example.lowcodekg.query.service.util.retriever;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.lowcodekg.query.utils.Constants.*;

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
    private LLMService llmService;
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
            List<Node> nodeList;
            List<Document> documents = new ArrayList<>();
            StringBuilder taskInfo = new StringBuilder();
            for(IR ir: task.getIrList()) {
                taskInfo.append(ir.toSentence() + " ");
            }
            if(debugConfig.isDebugMode()) {
                System.out.println("子任务检索信息:\n" + taskInfo + "\n");
            }

            // 基于ES向量检索，获取候选列表
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(taskInfo.toString()));

            if(task.getIsPage())  {
                documents.addAll(esService.hybridSearch(
                        taskInfo.toString(), vector,
                        MAX_PAGE_NUM, MIN_SCORE,
                        0.0,
                        PAGE_INDEX_NAME
                ));
            }
            if(task.getIsWorkflow()) {
                documents.addAll(esService.hybridSearch(
                        taskInfo.toString(), vector,
                        MAX_WORKFLOW_NUM, MIN_SCORE,
                        0.0,
                        WORKFLOW_INDEX_NAME
                ));
            }
            if(task.getIsData()) {
                documents.addAll(esService.hybridSearch(
                        taskInfo.toString(), vector,
                        MAX_DATA_OBJECT_NUM, MIN_SCORE,
                        0.0,
                        DATA_OBJECT_INDEX_NAME
                ));
            }

            // 将 Document 转换为 Node 并返回 Result
            nodeList = documents.stream()
                    .map(this::convertToNeo4jNode)
                    .collect(Collectors.toList());

            return Result.build(nodeList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in queryEntitiesBySubTask: " + e.getMessage());
            e.printStackTrace();
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private List<Node> queryCategoryEntitiesByTask(String query, String indexName) {
        List<Node> pageEntities = new ArrayList<>();
        try {
            // 生成查询向量
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(query));

            List<Document> documents = esService.hybridSearch(
                    query,              // 文本查询
                    vector,             // 向量查询
                    MAX_RESULTS,        // 最大返回结果数
                    MIN_SCORE,          // 最小分数阈值
                    0.1,                // 文本搜索权重
                    indexName           // 索引名称
            );

            // 将 Document 转换为 Node
            pageEntities = documents.stream()
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
        node.setId(Long.valueOf(document.getId()));
        node.setName(document.getName());
        node.setFullName(document.getFullName());
        node.setLabel(document.getLabel());
        node.setDescription(document.getContent());
        node.setIrList(JSONObject.parseArray(document.getIr(), IR.class));
        return node;
    }
}
