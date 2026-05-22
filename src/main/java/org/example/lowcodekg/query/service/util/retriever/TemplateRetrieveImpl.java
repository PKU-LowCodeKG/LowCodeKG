package org.example.lowcodekg.query.service.util.retriever;

import lombok.extern.slf4j.Slf4j;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.example.lowcodekg.query.utils.Constants.*;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:58
 */
@Slf4j
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
            log.error("Error in queryEntitiesByTask", e);
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    @Override
    public Result<List<Node>> queryBySubTask(Task task) {
        try {
            String taskInfo = task.getName() + " " + task.getDescription();
            if(debugConfig.isDebugMode()) {
                log.debug("子任务检索信息:\n{}\n", taskInfo);
            }

            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(taskInfo));
            List<Document> documents = esService.hybridSearch(
                    taskInfo, vector, MAX_CODE_ENTITY_NUM, MIN_SCORE, 0.0, CODE_ENTITY_INDEX_NAME);

            List<Node> nodeList = documents.stream()
                    .map(this::convertToNeo4jNode)
                    .collect(Collectors.toList());

            return Result.build(nodeList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            log.error("Error in queryEntitiesBySubTask", e);
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    @Override
    public Result<Map<Task, List<Node>>> queryBySubTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return Result.build(Collections.emptyMap(), ResultCodeEnum.SUCCESS);
        }

        try {
            // 批量嵌入：所有子任务文本一次编码
            List<String> taskTexts = tasks.stream()
                    .map(t -> t.getName() + " " + t.getDescription())
                    .collect(Collectors.toList());
            List<List<Float>> allVectors = EmbeddingUtil.embedTexts(taskTexts);

            // 并行 ES 查询
            List<CompletableFuture<AbstractMap.SimpleEntry<Task, List<Node>>>> futures = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                final Task task = tasks.get(i);
                final String taskText = taskTexts.get(i);
                final float[] vector = FormatUtil.ListToArray(allVectors.get(i));

                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Document> docs = esService.hybridSearch(
                                taskText, vector, MAX_CODE_ENTITY_NUM, MIN_SCORE, 0.0, CODE_ENTITY_INDEX_NAME);
                        List<Node> nodes = docs.stream()
                                .map(this::convertToNeo4jNode)
                                .collect(Collectors.toList());
                        return new AbstractMap.SimpleEntry<>(task, nodes);
                    } catch (Exception e) {
                        log.error("Error searching for subtask: {}", task.getName(), e);
                        return new AbstractMap.SimpleEntry<>(task, Collections.emptyList());
                    }
                }));
            }

            // 等待全部完成并收集结果
            Map<Task, List<Node>> resultMap = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toMap(
                            AbstractMap.SimpleEntry::getKey,
                            AbstractMap.SimpleEntry::getValue));

            return Result.build(resultMap, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            log.error("Error in batch queryBySubTasks", e);
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
