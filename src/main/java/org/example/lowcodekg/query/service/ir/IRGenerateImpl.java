package org.example.lowcodekg.query.service.ir;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.example.lowcodekg.query.utils.Prompt.RESOURCE_TO_IR_PROMPT;
import static org.example.lowcodekg.query.utils.Prompt.TASK_TO_IR_PROMPT;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:50
 */
@Service
public class IRGenerateImpl implements IRGenerate {

    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private DebugConfig debugConfig;

    @Override
    public Result<List<IR>> convertTaskToIR(Task task) {
        try {
            String taskInfo = task.getName() + "\n" + task.getDescription();
            String prompt = TASK_TO_IR_PROMPT.replace("{Task}", taskInfo);
            String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
            List<IR> irList = buildIRList(answer);
            return Result.build(irList, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error occurred while converting task to IR: " + e.getMessage());
            throw new RuntimeException("Error occurred while converting task to IR: " + e.getMessage());
        }
    }

    @Override
    public Result<List<IR>>  convertTemplateToIR(Node template) {
        try {
            String label = template.getLabel();
            List<IR> irList = new ArrayList<>();
            if("Workflow".equals(label)) {
                StringBuilder templateInfo = new StringBuilder();
                templateInfo.append("模板名称：").append(template.getName()).append("\n")
                        .append("模板描述：").append(template.getDescription()).append("\n")
                        .append("模板代码：").append(template.getContent()).append("\n");
                String prompt = RESOURCE_TO_IR_PROMPT.replace("{Task}", templateInfo.toString());
                String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
                if(debugConfig.isDebugMode()) {
                    System.out.println("模板转换prompt:\n" + prompt + "\n");
                    System.out.println("模板转换结果:\n" + answer + "\n");
                }
                irList = buildIRList(answer);
            } else if("PageTemplate".equals(label)) {
                IR ir = new IR();
                ir.setObject(template.getName());
                ir.setCondition(template.getDescription());
                irList.add(ir);
            } else if("DataObject".equals(label)) {
                IR ir  = new IR();
                ir.setObject(template.getName());
                ir.setCondition(template.getDescription());
                irList.add(ir);
            }

            return Result.build(irList, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error occurred while converting template to IR: " + e.getMessage());
            throw new RuntimeException("Error occurred while converting template to IR: " + e.getMessage());
        }
    }

    private List<IR> buildIRList(String answer) {
        try {
            JSONObject jsonObject = JSON.parseObject(answer);
            JSONArray irArray = jsonObject.getJSONArray("IR");
            List<IR> isList = new ArrayList<>();
            for(int i = 0; i < irArray.size(); i++) {
                JSONObject irObject = irArray.getJSONObject(i);
                isList.add(new IR(
                        irObject.getString("action"),
                        irObject.getString("object"),
                        irObject.getString("target"),
                        irObject.getString("condition")
                ));
            }
            return isList;
        } catch (JSONException e) {
            System.err.println("Error parsing JSON in TaskToDsl: " + e.getMessage());
            throw new RuntimeException("Error parsing JSON: " + e.getMessage());
        }
    }
}
