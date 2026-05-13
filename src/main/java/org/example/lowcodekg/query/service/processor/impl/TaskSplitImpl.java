package org.example.lowcodekg.query.service.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.SubTaskIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TaskSplitImpl implements TaskSplit {
    @Autowired
    private SubTaskIndexService subTaskIndexService;

    private static final List<String> ALL_CATEGORIES = Arrays.asList("page", "workflow", "data");

    @Override
    public Result<TaskGraph> taskSplit(String query) {
        try {
            TaskGraph graph = new TaskGraph();

            log.info("==== 阶段一：需求分解 ====");
            log.info("查询: {}", query);

            // 搜索最匹配的任务模板
            List<Document> matchedDocs = subTaskIndexService.searchSubTasks(query);

            if (matchedDocs.isEmpty()) {
                // 未命中任何模板，使用原始查询作为 fallback
                log.info("  未命中模板，使用原始查询直接检索");
                Task fallbackTask = new Task(
                        "fallback_0",
                        query,
                        ALL_CATEGORIES,
                        query,
                        new ArrayList<>());
                graph.addTask(fallbackTask);
            } else {
                // 命中模板，使用模板的子任务列表
                List<Task> taskList = buildTaskList(matchedDocs);
                for (int i = 0; i < taskList.size(); i++) {
                    Task task = taskList.get(i);
                    graph.addTask(task);
                    log.info("  子任务 {}: {} | {}", i + 1, task.getName(), task.getDescription());
                }
            }

            return Result.build(graph, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            log.error("Error occurred while splitting the task", e);
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private List<Task> buildTaskList(List<Document> documents) {
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String id = doc.getName() + "_" + doc.getId();
            taskList.add(new Task(
                    id,
                    doc.getName(),
                    ALL_CATEGORIES,
                    doc.getContent(),
                    new ArrayList<>()));
        }
        return taskList;
    }
}
