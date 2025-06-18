package org.example.lowcodekg.query.service.processor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.example.lowcodekg.query.utils.Prompt.*;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:55
 */
@Service
public class TaskSplitImpl implements TaskSplit {
    @Autowired
    private LLMService llmService;
    @Autowired
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private IRGenerate irGenerate;
    @Autowired
    private DebugConfig debugConfig;

    @Override
    public Result<TaskGraph> taskSplit(String query) {
        try {
            TaskGraph graph = new TaskGraph();

            // 根据需求检索相关资源
            List<Node> nodes = templateRetrieve.queryByTask(query).getData();
            if(debugConfig.isDebugMode()) {
                System.out.println("初步检索资源:\n" + nodes);
            }

            // 基于检索结果，构造提示让LLM进行任务分解，并识别依赖
            String answer = getSplitTasks(query, nodes);
            if(debugConfig.isDebugMode()) {
                System.out.println("LLM任务分解:\n" + answer);
            }

            // 将返回json格式字符串解析为Task对象
            List<Task> taskList = buildTaskList(answer);

            // 针对每个子任务，生成相应的IR（功能原语），记录在Task属性中
            for(Task task : taskList) {
                task.setIrList(irGenerate.convertTaskToIR(task).getData());
                graph.addTask(task);
                if(debugConfig.isDebugMode()) {
                    System.out.println("Task: " + task.getName() + " IR: " + task.getIrList());
                }
            }

            // 基于分解后的任务，让LLM判断子任务之间的依赖约束关系
//            String result = identifyDependenciesBetweenTasks(graph, query);
//            if(debugConfig.isDebugMode()) {
//                System.out.println("任务依赖关系识别: " + result);
//            }

            // 构建子任务依赖图
            buildDependencyGraph(graph, answer);

            return Result.build(graph, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while splitting the task: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private void buildDependencyGraph(TaskGraph graph, String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray dependencies = jsonObject.getJSONArray("dependencies");

            for (int i = 0; i < dependencies.size(); i++) {
                JSONObject dependency = dependencies.getJSONObject(i);
                String desciption = "";
                String sourceTaskId = "";
                String targetTaskId = "";
                for (String key : dependency.keySet()) {
                    if ("dependency".equals(key)) {
                        desciption = dependency.getString(key);
                    } else {
                        sourceTaskId = key;
                        targetTaskId = dependency.getString(key);
                    }
                }
                // 添加依赖关系以及对应的描述
                if(debugConfig.isDebugMode()) {
                    System.out.println("Dependency: " + sourceTaskId + " -> " + targetTaskId + " : " + desciption);
                }
                graph.addDependency(sourceTaskId, targetTaskId, desciption);
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON in buildDependencyGraph: " + e.getMessage());
            throw new RuntimeException("Error parsing JSON in : buildDependencyGraph" + e.getMessage());
        }
    }

    private String identifyDependenciesBetweenTasks(TaskGraph graph, String query) {
        try {
            StringBuilder taskInfos = new StringBuilder();
            for(Task task : graph.getTasks().values()) {
                taskInfos.append(task.toString() + "\n");
            }
            String prompt = IDENTIFY_TASK_DEPENDENCY_PROMPT.replace("{query}", query).replace("{subTasks}", taskInfos.toString());
            String result = FormatUtil.extractJson(llmService.chat(prompt));

            return result;

        } catch (Exception e) {
            System.err.println("Error occurred while identifying dependencies between tasks: " + e.getMessage());
            throw new RuntimeException("Error occurred while identifying dependencies between tasks: " + e.getMessage());
        }
    }

    private String getSplitTasks(String query, List<Node> nodes) {
        JSONArray jsonArray = new JSONArray();
        for(Node node : nodes) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", node.getName());
            jsonObject.put("content", node.getContent());
            jsonObject.put("description", node.getDescription());
            jsonArray.add(jsonObject);
        }
        String codePrompt = jsonArray.toJSONString();
        String prompt = TASK_GRAPH_BUILD_PROMPT.replace("{code}", codePrompt).replace("{task}", query);
        if(debugConfig.isDebugMode()) {
            System.out.println("Task split prompt:\n" + prompt);
        }
        String answer = FormatUtil.extractJson(llmService.chat(prompt));
        return answer;
    }

    private List<Task> buildTaskList(String answer) {
        try {
            List<Task> tasksList = new ArrayList<>();
            JSONObject jsonObject = JSON.parseObject(answer);
            JSONArray subtasksArray = jsonObject.getJSONArray("subtasks");
            for(int i = 0; i < subtasksArray.size(); i++) {
                JSONObject subtaskObject = subtasksArray.getJSONObject(i);
                JSONArray categoryArray = subtaskObject.getJSONArray("category");
                List<String> categories = new ArrayList<>();
                for(int j = 0; j < categoryArray.size(); j++) {
                    categories.add(categoryArray.getString(j));
                }
                
                tasksList.add(new Task(
                        subtaskObject.getString("name") + "_" + subtaskObject.getString("id"),
                        subtaskObject.getString("name"),
                        categories,
                        subtaskObject.getString("description"),
                        new ArrayList<>()));
            }
            return tasksList;
        } catch (JSONException e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            throw new RuntimeException("Error parsing JSON: " + e.getMessage());
        }
    }
}
