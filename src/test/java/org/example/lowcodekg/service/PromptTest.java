package org.example.lowcodekg.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.service.util.LLMService;
import org.example.lowcodekg.query.service.util.ir.IRGenerate;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class PromptTest {
    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private LLMService llm;
    @Autowired
    private IRGenerate irGenerate;

    @Test
    void test() {
        String prompt = """
            You are an expert in Natural Language Processing specializing in structured information extraction. 
            Your task is to analyze input text and decompose it into discrete operational components using the DSL schema.
            
            **Task**:
            Extract ALL actionable instructions from the input text and map them to a list of DSL objects. Each DSL must represent a SINGLE atomic operation with explicit contextual dependencies.
            
            **DSL Field Definitions**:
               - `action`: *Required* - The core verb/operation (e.g., "filter", "sort", "export").
               - `object`: *Required* - The entity being manipulated (e.g., "raw_data", "user_logs").
               - `target`: *Optional* - The output destination/format (e.g., "CSV_file", "database_table").
               - `condition`: *Optional* - Environmental constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance").
            
            **Input**:
            You will be provided with a natural language description that describes a functional requirement, the task description is:
            实现服务层方法:创建或更新一个服务层方法，用于处理博客置顶状态的更新逻辑。
            
            **Output**:
               - Your task is to identify and extract the relevant information from the description and organize it into DSL objects.
               - Return the extracted DSL objects in the following JSON format in Chinese:
            
            ```json
            {
                "IR": [
                    {
                        "action": "<动作1>",
                        "object": "<对象1>",
                        "target": "<目标1>",
                        "condition": "<条件1>"
                    },
                    {
                        "action": "<操作2>",
                        "object": "<对象2>",
                        "target": "<目标2>",
                        "condition": "<条件2>"
                    },
                    ...
                ]
            }
            ```
            
            **Instructions**:
               - Carefully read the natural language description to identify actions, objects, targets, and conditions.
               - Ensure that each extracted DSL object accurately reflects the information in the description.
               - If any component (action, object, target, condition) is not explicitly mentioned, use "null" as placeholder.
               - The JSON output must strictly follow the provided format in Chinese.
            """;

        long startTime1 = System.currentTimeMillis();
        String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
        System.out.println(answer);
        long endTime1 = System.currentTimeMillis();
        System.out.println("llmService.generateAnswer(prompt) 执行时间: " + (endTime1 - startTime1) + " ms");

        List<IR> irList = irGenerate.buildIRList(answer);
        for (IR ir : irList) {
            System.out.println(ir.toSentence());
        }

//        long startTime2 = System.currentTimeMillis();
//        System.out.println(llm.chat(prompt));
//        long endTime2 = System.currentTimeMillis();
//        System.out.println("llm.chat(prompt) 执行时间: " + (endTime2 - startTime2) + " ms");
    }
}
