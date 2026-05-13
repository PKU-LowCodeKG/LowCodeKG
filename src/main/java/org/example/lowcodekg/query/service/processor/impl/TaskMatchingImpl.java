package org.example.lowcodekg.query.service.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.lowcodekg.query.utils.Constants.MAX_RESOURCE_RECOMMEND_NUM;

/**
 * @author Sherloque
 * @Date 2025/3/26
 */
@Slf4j
@Service
public class TaskMatchingImpl implements TaskMatching {

    @Autowired
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private DebugConfig debugConfig;

    @Override
    public Result<Void> rerankResource(Task task) {
        try {
            List<Node> nodeList = templateRetrieve.queryBySubTask(task).getData();
            if (debugConfig.isDebugMode()) {
                log.debug("子任务名称：{}:{}", task.getName(), task.getDescription());
                log.debug("子任务检索结果个数: {}", nodeList.size());
                log.debug("子任务检索资源:");
                for (Node node : nodeList) {
                    log.debug("{}", node);
                }
            }

            int limit = MAX_RESOURCE_RECOMMEND_NUM;
            List<Node> selectedNodes = nodeList.size() > limit
                    ? nodeList.subList(0, limit)
                    : nodeList;

            task.setResourceList(selectedNodes);

            if (selectedNodes.isEmpty()) {
                log.info("[{}] 无匹配资源（低于阈值），跳过", task.getName());
            } else {
                log.info("[{}] 匹配 {} 个资源:", task.getName(), selectedNodes.size());
                for (Node node : selectedNodes) {
                    log.info("  - {} ({})", node.getName(), node.getLabel());
                }
            }

            if (debugConfig.isDebugMode()) {
                log.debug("筛选后的资源个数: {}", selectedNodes.size());
                log.debug("筛选后的资源:");
                for (Node node : selectedNodes) {
                    log.debug("{}", node);
                }
            }

            return Result.build(null, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            log.error("Error occurred while reranking resources", e);
            throw new RuntimeException("Error occurred while reranking resources: " + e.getMessage());
        }
    }

}
