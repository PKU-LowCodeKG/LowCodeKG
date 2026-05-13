package org.example.lowcodekg.query.service.processor.impl;

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
                System.out.println("子任务名称：" + task.getName() + ":" + task.getDescription());
                System.out.println("子任务检索结果个数:" + nodeList.size());
                System.out.println("子任务检索资源:");
                for (Node node : nodeList) {
                    System.out.println(node);
                }
            }

            int limit = MAX_RESOURCE_RECOMMEND_NUM;
            List<Node> selectedNodes = nodeList.size() > limit
                    ? nodeList.subList(0, limit)
                    : nodeList;

            task.setResourceList(selectedNodes);
            if (debugConfig.isDebugMode()) {
                System.out.println("筛选后的资源个数: " + selectedNodes.size());
                System.out.println("筛选后的资源:");
                for (Node node : selectedNodes) {
                    System.out.println(node);
                }
            }

            return Result.build(null, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while reranking resources: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error occurred while reranking resources: " + e.getMessage());
        }
    }

}
