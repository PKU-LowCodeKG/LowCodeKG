package org.example.lowcodekg.query.service.decomposer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.ir.DslGenerate;
import org.example.lowcodekg.query.service.retriever.TemplateRetrieve;
import org.example.lowcodekg.service.ElasticSearchService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    private DslGenerate dslGenerate;

    @Override
    public Result<TaskGraph> taskSplit(String query) {
        try {
            TaskGraph graph = new TaskGraph();

            // 根据需求检索相关资源
            List<Node> nodes = templateRetrieve.queryEntitiesByTask(query).getData();

            // 基于检索结果，构造提示让LLM进行任务分解
            String codePrompt = buildCodePrompt(nodes);
            String prompt = TaskSplitPrompt.replace("{code}", codePrompt).replace("{task}", query);
            String answer = llmService.generateAnswer(prompt);
            if(answer.contains("```json")) {
                answer = answer.substring(answer.indexOf("```json") + 7, answer.lastIndexOf("```"));
            } else {
                throw new RuntimeException("Task split result json format error:\n" + answer);
            }

            // 将返回json格式字符串解析为Task对象
            List<Task> taskList = buildTaskList(answer);

            // 针对每个子任务，生成相应的IR（功能原语），记录在Task属性中
            for(Task task : taskList) {
                task.setDslList(dslGenerate.convertTaskToIR(task).getData());
            }

            // 基于分解后的任务，让LLM判断子任务之间的依赖约束关系，形成子任务依赖图


            return Result.build(graph, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while splitting the task: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }
    }

    private String buildCodePrompt(List<Node> nodes) {
        JSONArray jsonArray = new JSONArray();
        for(Node node : nodes) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", node.getName());
            jsonObject.put("label", node.getLabel());
            jsonObject.put("content", node.getContent());
            jsonObject.put("description", node.getDescription());
            jsonArray.add(jsonObject);
        }
        return jsonArray.toJSONString();
    }

    private List<Task> buildTaskList(String answer) {
        try {
            List<Task> tasksList = new ArrayList<>();
            JSONObject jsonObject = JSON.parseObject(answer);
            JSONArray subtasksArray = jsonObject.getJSONArray("subtasks");
            for(int i = 0; i < subtasksArray.size(); i++) {
                JSONObject subtaskObject = subtasksArray.getJSONObject(i);
                tasksList.add(new Task(
                        subtaskObject.getString("id"),
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
        // JSON字符串
        String jsonString = "{\n" +
                "                \"subtasks\": [\n" +
                "                    {\n" +
                "                        \"id\": 1,\n" +
                "                        \"name\": \"subtask1\",\n" +
                "                        \"description\": \"subtask1 description\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"id\": 2,\n" +
                "                        \"name\": \"subtask2\",\n" +
                "                        \"description\": \"subtask2 description\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }";

    }
}
