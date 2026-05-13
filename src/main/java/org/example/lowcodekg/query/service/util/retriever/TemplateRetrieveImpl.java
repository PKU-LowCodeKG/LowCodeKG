package org.example.lowcodekg.query.service.util.retriever;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private DebugConfig debugConfig;


    @Override
    public Result<List<Node>> queryByTask(String query) {
        try {
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(query));
            List<Document> documents = esService.hybridSearch(
                    query, vector, MAX_CODE_ENTITY_NUM, MIN_SCORE, 0.1, CODE_ENTITY_INDEX_NAME);
            List<Node> nodes = documents.stream()
                    .map(this::convertToNeo4jNode)
                    .collect(Collectors.toList());
            return Result.build(nodes, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error in queryEntitiesByTask: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    @Override
    public Result<List<Node>> queryBySubTask(Task task) {
        try {
            String taskInfo = task.getName() + " " + task.getDescription();
            if(debugConfig.isDebugMode()) {
                System.out.println("子任务检索信息:\n" + taskInfo + "\n");
            }

            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(taskInfo));
            List<Document> documents = esService.hybridSearch(
                    taskInfo, vector, MAX_CODE_ENTITY_NUM, MIN_SCORE, 0.0, CODE_ENTITY_INDEX_NAME);

            List<Node> nodeList = documents.stream()
                    .map(this::convertToNeo4jNode)
                    .collect(Collectors.toList());

            return Result.build(nodeList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in queryEntitiesBySubTask: " + e.getMessage());
            e.printStackTrace();
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private Node convertToNeo4jNode(Document document) {
        // 根据 Document 的属性创建 Neo4jNode
        Node node = new Node();
        node.setId(Long.valueOf(document.getId()));
        node.setName(document.getName());
        node.setFullName(document.getFullName());
        node.setLabel(document.getLabel());
        node.setDescription(document.getContent());
        return node;
    }
}
