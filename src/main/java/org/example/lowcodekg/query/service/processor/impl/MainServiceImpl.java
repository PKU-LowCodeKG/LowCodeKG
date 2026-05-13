package org.example.lowcodekg.query.service.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.MainService;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.lowcodekg.query.utils.Constants.SAVE_EM_RESULT_PATH;
import static org.example.lowcodekg.query.utils.Constants.logFilePath;
import static org.example.lowcodekg.query.utils.FormatUtil.saveResult;

@Slf4j
@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private TaskSplit taskSplit;
    @Autowired
    private TaskMatching taskMatching;
    @Autowired
    private TaskMerge taskMerge;

    @Override
    public Result<Void> recommendList(List<String> queryList, String savePath) {
        try {
            for(String query: queryList) {
                TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
                for(Task task : taskGraph.getTasks().values()) {
                    taskMatching.rerankResource(task);
                }
                Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
                saveResult(query, resourceList, savePath);
            }
        } catch(Exception e) {
            log.error("Error in recommendList", e);
            throw new RuntimeException("Error in recommendList: " + e.getMessage());
        }
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Override
    public Result<List<Node>> recommend(String query) {
        try {
            Map<Task, Set<Node>> resourceList;

            // 阶段一：检索增强的需求分解
            TaskGraph taskGraph = taskSplit.taskSplit(query).getData();

            // 阶段二：基于IR的需求-资源匹配并重排序
            log.info("==== 阶段二：资源匹配 ====");
            for(Task task : taskGraph.getTasks().values()) {
                taskMatching.rerankResource(task);
            }

            // 任务合并
            resourceList = taskMerge.mergeTask(taskGraph, query).getData();
            List<Node> nodeList = new ArrayList<>();
            for(Task task : resourceList.keySet()) {
                nodeList.addAll(resourceList.get(task));
            }
            return Result.build(new ArrayList<>(new HashSet<>(nodeList)), ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            log.error("Error in recommend", e);
            throw new RuntimeException("Error in recommend: " + e.getMessage());
        }
    }
}
