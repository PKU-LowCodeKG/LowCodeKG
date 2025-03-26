package org.example.lowcodekg.query.service.processor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.example.lowcodekg.query.utils.Prompt.IDENTIFY_TASK_DEPENDENCY_PROMPT;
import static org.example.lowcodekg.query.utils.Prompt.TaskSplitPrompt;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:55
 */
@Service
public class TaskSplitImpl implements TaskSplit {
    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private IRGenerate dslGenerate;

    @Override
    public Result<TaskGraph> taskSplit(String query) {
        try {
            TaskGraph graph = new TaskGraph();

            // 根据需求检索相关资源
            List<Node> nodes = templateRetrieve.queryByTask(query).getData();

            // 基于检索结果，构造提示让LLM进行任务分解
            String answer = getSplitTasks(query, nodes);

            // 将返回json格式字符串解析为Task对象
            List<Task> taskList = buildTaskList(answer);

            // 针对每个子任务，生成相应的IR（功能原语），记录在Task属性中
            for(Task task : taskList) {
                task.setDslList(dslGenerate.convertTaskToIR(task).getData());
                graph.addTask(task);
            }

            // 基于分解后的任务，让LLM判断子任务之间的依赖约束关系
            String result = identifyDependenciesBetweenTasks(graph, query);

            // 构建子任务依赖图
            buildDependencyGraph(graph, result);

            return Result.build(graph, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while splitting the task: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private void buildDependencyGraph(TaskGraph graph, String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray dependencies = jsonObject.getJSONArray("Dependencies");

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
            String result = FormatUtil.extractJson(llmService.generateAnswer(prompt));

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
            jsonObject.put("label", node.getLabel());
            jsonObject.put("content", node.getContent());
            jsonObject.put("description", node.getDescription());
            jsonArray.add(jsonObject);
        }
        String codePrompt = jsonArray.toJSONString();
        String prompt = TaskSplitPrompt.replace("{code}", codePrompt).replace("{task}", query);
        String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
        return answer;
    }

    private List<Task> buildTaskList(String answer) {
        try {
            List<Task> tasksList = new ArrayList<>();
            JSONObject jsonObject = JSON.parseObject(answer);
            JSONArray subtasksArray = jsonObject.getJSONArray("subtasks");
            for(int i = 0; i < subtasksArray.size(); i++) {
                JSONObject subtaskObject = subtasksArray.getJSONObject(i);
                tasksList.add(new Task(
                        subtaskObject.getString("name") + "_" + subtaskObject.getString("id"),
                        subtaskObject.getString("name"),
                        subtaskObject.getString("description"),
                        new ArrayList<>()));
            }
            return tasksList;
        } catch (JSONException e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            throw new RuntimeException("Error parsing JSON: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 创建 DSL 对象
        IR dsl1 = new IR("CREATE", "Entity1", "Database", "Condition1");
        IR dsl2 = new IR("UPDATE", "Entity2", "Database", "Condition2");

        // 创建 Task 对象
        Task task = new Task("task1", "Create and Update Entities", "This task involves creating and updating entities", new ArrayList<>(List.of(dsl1, dsl2)));

        // 打印 Task 对象
        System.out.println(task.toString());
    }
}
