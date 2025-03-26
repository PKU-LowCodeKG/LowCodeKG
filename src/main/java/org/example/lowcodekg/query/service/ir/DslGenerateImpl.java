package org.example.lowcodekg.query.service.ir;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.DSL;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.example.lowcodekg.query.utils.Prompt.TASK_TO_IR_PROMPT;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:50
 */
@Service
public class DslGenerateImpl implements DslGenerate {

    @Autowired
    private LLMGenerateService llmService;

    @Override
    public Result<List<DSL>> convertTaskToIR(Task task) {

        String taskInfo = task.getName() + "\n" + task.getDescription();
        String prompt = TASK_TO_IR_PROMPT.replace("{Task}", taskInfo);
        String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
        List<DSL> dslList = buildDslList(answer);

        return Result.build(dslList, ResultCodeEnum.SUCCESS);
    }

    @Override
    public Result<List<DSL>> convertTemplateToIR(String template) {
        return null;
    }

    private List<DSL> buildDslList(String answer) {
        try {
            JSONObject jsonObject = JSON.parseObject(answer);
            JSONArray irArray = jsonObject.getJSONArray("IR");
            List<DSL> dslList = new ArrayList<>();
            for(int i = 0; i < irArray.size(); i++) {
                JSONObject irObject = irArray.getJSONObject(i);
                dslList.add(new DSL(
                        irObject.getString("action"),
                        irObject.getString("object"),
                        irObject.getString("target"),
                        irObject.getString("condition")
                ));
            }
            return dslList;
        } catch (JSONException e) {
            System.err.println("Error parsing JSON in TaskToDsl: " + e.getMessage());
            throw new RuntimeException("Error parsing JSON: " + e.getMessage());
        }
    }
}
