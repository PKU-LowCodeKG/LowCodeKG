package org.example.lowcodekg.query.service.ir;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.micrometer.common.util.StringUtils;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
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
public class IRGenerateImpl implements IRGenerate {

    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private DebugConfig debugConfig;

    @Override
    public Result<List<IR>> generateIR(String description, String type) {
        try {
            String prompt = TASK_TO_IR_PROMPT.replace("{Task}", description);
            String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
            List<IR> irList = buildIRList(answer);
            irList.stream().forEach(ir -> {
                ir.setType(type);
            });
            return Result.build(irList, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while generating IR: " + e.getMessage());
            throw new RuntimeException("Error occurred while generating IR: " + e.getMessage());
        }
    }

    @Override
    public Result<List<IR>> convertTaskToIR(Task task) {
        try {
            String taskInfo = task.getName() + ":" + task.getDescription();
            String prompt = TASK_TO_IR_PROMPT.replace("{Task}", taskInfo);
            String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
            List<IR> irList = buildIRList(answer);
            task.setIrList(irList);

            return Result.build(irList, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error occurred while converting task to IR: " + e.getMessage());
            throw new RuntimeException("Error occurred while converting task to IR: " + e.getMessage());
        }
    }

    @Override
    public Result<List<IR>> convertTemplateToIR(Node template) {
        try {
            String label = template.getLabel();
            List<IR> irList = new ArrayList<>();
            if("Workflow".equals(label)) {
                String templateInfo = template.getName() + ":" + template.getDescription();
                String prompt = TASK_TO_IR_PROMPT.replace("{Task}", templateInfo);
                String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
                irList = buildIRList(answer);
            } else if("PageTemplate".equals(label)) {
                IR ir = new IR();
                ir.setObject(template.getName());
                ir.setCondition(template.getDescription());
                ir.setType("PageTemplate");
                irList.add(ir);
            } else if("DataObject".equals(label)) {
                IR ir  = new IR();
                ir.setObject(template.getName());
                ir.setCondition(template.getDescription());
                ir.setType("DataObject");
                irList.add(ir);
            }
            template.setIrList(irList);

            return Result.build(irList, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error occurred while converting template to IR: " + e.getMessage());
            throw new RuntimeException("Error occurred while converting template to IR: " + e.getMessage());
        }
    }

    @Override
    public List<IR> buildIRList(String answer) {
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
                        irObject.getString("condition"),
                        null
                ));
            }
            return isList;
        } catch (JSONException e) {
            System.err.println("Error parsing JSON in TaskToDsl: " + e.getMessage());
            throw new RuntimeException("Error parsing JSON: " + e.getMessage());
        }
    }

    /**
     * @param ir1 任务IR
     * @param ir2 资源IR
     */
    @Override
    public Double calculateIRSim(IR ir1, IR ir2) {
        // 添加规则化处理逻辑
//        IR ir1Edit = new IR(ir1);
//        if(StringUtils.isNotBlank(ir2.getType()) && !"Workflow".equals(ir2.getType())) {
//            ir1Edit.setAction(null);
//        }
        String ir1Sentence = ir1.toSentence();
        String ir2Sentence = ir2.toSentence();

        List<String> words1 = FormatUtil.textPreProcess(ir1Sentence);
        List<String> words2 = FormatUtil.textPreProcess(ir2Sentence);
        Double wordSimilarity = FormatUtil.calculateWordLevelSimilarity(words1, words2);
        // 向量相似度
        float[] ir1Vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(ir1Sentence));
        float[] ir2Vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(ir2Sentence));
        double embeddingSimilarity = EmbeddingUtil.cosineSimilarity(ir1Vector, ir2Vector);
        double sim = 0.1 * wordSimilarity + 0.9 * embeddingSimilarity;
        return sim;
    }
}
