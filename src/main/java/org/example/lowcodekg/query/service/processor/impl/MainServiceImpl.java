package org.example.lowcodekg.query.service.processor.impl;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.MainService;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private TaskSplit taskSplit;
    @Autowired
    private TaskMatching taskMatching;
    @Autowired
    private TaskMerge taskMerge;

    @Override
    public Result<List<Node>> recommend(String query) {
        try {
            List<Node> resourceList;

            // 检索增强的需求分解
            TaskGraph taskGraph = taskSplit.taskSplit(query).getData();

            // 基于IR的需求-资源匹配并重排序
            for(Task task : taskGraph.getTasks().values()) {
                taskMatching.rerankResource(task);
            }

            // 任务合并
            resourceList = taskMerge.mergeTask(taskGraph, query).getData();

            return Result.build(resourceList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in recommend: " + e.getMessage());
            throw new RuntimeException("Error in recommend: " + e.getMessage());
        }
    }
}
