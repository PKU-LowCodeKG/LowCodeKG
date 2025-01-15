package org.example.lowcodekg.service.impl;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;
import org.example.lowcodekg.service.IndexingService;
import org.example.lowcodekg.service.Neo4jGraphService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private Neo4jGraphService neo4jGraphService;

    @Override
    public void exportJavaClassMethodToJson(String jsonPath) {
        JSONArray jsonArray = new JSONArray();
//        List<JavaClassEntity> javaClasses = javaClassRepo.findAll();
        List<JavaClassEntity> javaClasses = neo4jGraphService.findAllJavaClass();
        System.out.println("JavaClass实体数量：" + javaClasses.size());
        for (JavaClassEntity javaClass : javaClasses) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", javaClass.getId());
                jsonObject.put("fullName", javaClass.getFullName());
                jsonObject.put("projectName", javaClass.getProjectName());
                jsonObject.put("comment", javaClass.getComment());
                jsonObject.put("content", javaClass.getContent());
                jsonObject.put("description", "");
                jsonObject.put("label", "JavaClass");
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        List<JavaMethodEntity> javaMethods = javaMethodRepo.findAll();
        List<JavaMethodEntity> javaMethods = neo4jGraphService.findAllJavaMethod();
        System.out.println("JavaMethod实体数量：" + javaMethods.size());
        for (JavaMethodEntity javaMethod : javaMethods) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", javaMethod.getId());
                jsonObject.put("fullName", javaMethod.getFullName());
                jsonObject.put("projectName", javaMethod.getProjectName());
                jsonObject.put("comment", javaMethod.getComment());
                jsonObject.put("content", javaMethod.getContent());
                jsonObject.put("description", "");
                jsonObject.put("label", "JavaMethod");
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter file = new FileWriter(jsonPath)) {
            file.write(jsonArray.toString(4));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateJsonWithDescription(String inputFile, String outputFile) {
        try {
            FileReader fileReader = new FileReader(new File(inputFile));
            char[] buffer = new char[(int) new File(inputFile).length()];
            fileReader.read(buffer);
            fileReader.close();
            String jsonContent = new String(buffer);

            // 将 JSON 内容解析为 JSONArray
            JSONArray jsonArray = new JSONArray(jsonContent);

            FileWriter fileWriter = new FileWriter(new File(outputFile));
            fileWriter.write("[\n");

            // 遍历 JSONArray 并更新 description 字段
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                System.out.println(i);
                String description = jsonObject.get("description").toString();
                if (description.isEmpty()) {
                    description = generateDescription(jsonObject.get("content").toString());
                }
                jsonObject.put("description", description);
                fileWriter.write(jsonObject.toString(4));
                if (i < jsonArray.length() - 1) {
                    fileWriter.write(",\n");
                }
            }
            fileWriter.write("\n]");
            fileWriter.flush();
            fileWriter.close();

            System.out.println("JSON 文件处理完成，已写入到: " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String generateDescription(Object content) {
        String template = """
                你是一名程序员，正在阅读一个博客系统的后端代码，
                请为下面这段代码生成一句简要的中文描述信息。
                
                code:
                {code}
                
                description:
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("code", content));
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
        String answer = assistantMessage.getContent();
        System.out.println(answer);
        return answer;
    }
}
