package org.example.lowcodekg.query.service.processor.impl;

import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.SubTaskIndexService;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TaskSplitImpl implements TaskSplit {
    @Autowired
    private SubTaskIndexService subTaskIndexService;
    @Autowired
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private DebugConfig debugConfig;

    private static final List<String> ALL_CATEGORIES = Arrays.asList("page", "workflow", "data");

    @Override
    public Result<TaskGraph> taskSplit(String query) {
        try {
            TaskGraph graph = new TaskGraph();

            // 检索相关资源（用于调试参考）
            List<Node> nodes = templateRetrieve.queryByTask(query).getData();
            if (debugConfig.isDebugMode()) {
                System.out.println("初步检索资源:\n" + nodes);
            }

            // 搜索子任务索引，获取匹配的子任务描述
            List<Document> matchedDocs = subTaskIndexService.searchSubTasks(query);
            if (debugConfig.isDebugMode()) {
                System.out.println("子任务检索匹配数: " + matchedDocs.size());
            }

            // 将子任务文档转换为 Task 对象
            List<Task> taskList = buildTaskList(matchedDocs);

            for (Task task : taskList) {
                graph.addTask(task);
                if (debugConfig.isDebugMode()) {
                    System.out.println("Task: " + task.getName() + " | " + task.getDescription());
                }
            }

            return Result.build(graph, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while splitting the task: " + e.getMessage());
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
