package org.example.lowcodekg.query.service.processor.impl;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.lowcodekg.query.utils.Prompt.RERANK_WITHIN_TASK_PROMPT;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 21:02
 */
@Service
public class TaskMergeImpl implements TaskMerge {
    @Autowired
    private DebugConfig debugConfig;
    @Autowired
    private LLMGenerateService llmService;

    @Override
    public Result<List<Node>> mergeTask(TaskGraph graph) {
        try {
            Set<Node> result = new HashSet<>();
            // 任务依赖图拓扑遍历
            List<Task> sortedTasks = graph.topologicalSort();
            for(Task task : sortedTasks) {
                if(task.getResourceList().size() == 0) {
                    continue;
                }
                // rerank within subtask
                rerankWithinTask(task);

                // 当前任务满足兼容性要求，加入结果集
                result.addAll(task.getResourceList());
            }

            if(debugConfig.isDebugMode()) {
                System.out.println("合并后的结果集:\n" + result + "\n");
            }
            List<Node> nodeList = new ArrayList<>(result);
            return Result.build(nodeList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in mergeTask: " + e.getMessage());
            throw new RuntimeException("Error in mergeTask: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> rerankWithinTask(Task task) {
        try {
            List<Node> nodeList = task.getResourceList();
            String taskInfo = task.getName() + ":" + task.getDescription();
            StringBuilder resourceInfo = new StringBuilder();
            for(Node node: nodeList) {
                resourceInfo.append(node.toString()).append("\n");
            }
            // LLM decision
            String prompt = RERANK_WITHIN_TASK_PROMPT
                    .replace("{task}", taskInfo)
                    .replace("{resourceList}", resourceInfo.toString());
            String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
            if(debugConfig.isDebugMode()) {
                System.out.println("task: " + task.getDescription());
                System.out.println("rerankWithinTask answer:\n" + answer);
            }
            // select reranked resources by LLM


            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error in rerankWithinTask: " + e.getMessage());
            throw new RuntimeException("Error in rerankWithinTask: " + e.getMessage());
        }
    }
}
