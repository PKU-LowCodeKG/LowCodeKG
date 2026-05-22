package org.example.lowcodekg.query.service.util.summarize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.*;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.utils.Prompt;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Service
public class DescriptionGenerator {

    @Autowired
    private LLMService llmService;
    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private FuncGenerate funcGenerate;

    private static final int MAX_CODE_LENGTH = 2000;
    private static final String OUTPUT_PATH = "src/main/resources/data/descriptions.json";

    private final Map<String, JSONObject> descriptionMap = new LinkedHashMap<>();

    public void generateAll() {
        log.info("==== 开始批量生成 LLM 描述 ====");
        descriptionMap.clear();

        generateJavaClassDescriptions();
        generateJavaMethodDescriptions();
        generateWorkflowDescriptions();
        generatePageDescriptions();

        writeJsonFile();
        log.info("==== LLM 描述生成完成，共 {} 条，已写入 {} ====", descriptionMap.size(), OUTPUT_PATH);
    }

    private void generateJavaClassDescriptions() {
        List<JavaClassEntity> entities = javaClassRepo.findAll();
        log.info("处理 JavaClass: {} 个", entities.size());
        int count = 0;
        for (JavaClassEntity entity : entities) {
            String code = entity.getContent();
            if (code == null || code.isBlank()) {
                continue;
            }
            try {
                String prompt = Prompt.WORKFLOW_SUMMARIZE_PROMPT.replace("{code}", truncate(code));
                String response = llmService.chat("You are a code analysis expert.", prompt);
                String desc = parseFunctionality(response);

                entity.setDescription(desc);
                javaClassRepo.save(entity);
                funcGenerate.genDataObjectFunc(entity);

                addToMap(entity.getFullName(), entity.getName(), desc, "JavaClass");
                count++;
                log.info("  JavaClass [{}/{}] {} → {}", count, entities.size(), entity.getName(), desc);
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Failed to generate description for JavaClass: {}", entity.getFullName(), e);
            }
        }
        log.info("JavaClass 完成: {}/{}", count, entities.size());
    }

    private void generateJavaMethodDescriptions() {
        List<JavaMethodEntity> entities = javaMethodRepo.findAll();
        log.info("处理 JavaMethod: {} 个", entities.size());
        int count = 0;
        for (JavaMethodEntity entity : entities) {
            String code = entity.getContent();
            if (code == null || code.isBlank()) {
                continue;
            }
            try {
                String prompt = Prompt.WORKFLOW_SUMMARIZE_PROMPT.replace("{code}", truncate(code));
                String response = llmService.chat("You are a code analysis expert.", prompt);
                String desc = parseFunctionality(response);

                entity.setDescription(desc);
                javaMethodRepo.save(entity);
                funcGenerate.genMethodFunc(entity);

                addToMap(entity.getFullName(), entity.getName(), desc, "JavaMethod");
                count++;
                log.info("  JavaMethod [{}/{}] {} → {}", count, entities.size(), entity.getName(), desc);
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Failed to generate description for JavaMethod: {}", entity.getFullName(), e);
            }
        }
        log.info("JavaMethod 完成: {}/{}", count, entities.size());
    }

    private void generateWorkflowDescriptions() {
        List<WorkflowEntity> entities = workflowRepo.findAll();
        log.info("处理 Workflow: {} 个", entities.size());
        int count = 0;
        for (WorkflowEntity entity : entities) {
            String code = entity.getContent();
            if (code == null || code.isBlank()) {
                continue;
            }
            try {
                String prompt = Prompt.WORKFLOW_SUMMARIZE_PROMPT.replace("{code}", truncate(code));
                String response = llmService.chat("You are a code analysis expert.", prompt);
                String desc = parseFunctionality(response);

                entity.setDescription(desc);
                workflowRepo.save(entity);
                funcGenerate.genWorkflowFunc(entity);

                addToMap(entity.getFullName(), entity.getName(), desc, "Workflow");
                count++;
                log.info("  Workflow [{}/{}] {} → {}", count, entities.size(), entity.getName(), desc);
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Failed to generate description for Workflow: {}", entity.getFullName(), e);
            }
        }
        log.info("Workflow 完成: {}/{}", count, entities.size());
    }

    private void generatePageDescriptions() {
        List<PageEntity> entities = pageRepo.findAll();
        log.info("处理 Page: {} 个", entities.size());
        int count = 0;
        for (PageEntity pageEntity : entities) {
            try {
                String formattedId = String.format("%d", pageEntity.getId());
                StringBuilder codeContent = new StringBuilder();
                Set<String> keywords = new HashSet<>();

                if (pageEntity.getContent() != null) {
                    codeContent.append(pageEntity.getContent()).append("\n");
                }

                // 查询关联的 Component
                String cypher = MessageFormat.format("""
                        MATCH (p:PageTemplate)-[:CONTAIN]->(c:Component)
                        WHERE id(p)={0}
                        RETURN c
                        """, formattedId);

                QueryRunner runner = neo4jClient.getQueryRunner();
                Result result = runner.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    Node node = record.get("c").asNode();
                    Map<String, Object> props = node.asMap();
                    Object compContent = props.get("content");
                    if (compContent != null) {
                        codeContent.append(compContent.toString()).append("\n");
                    }
                    Object text = props.get("text");
                    if (text != null) {
                        keywords.add(text.toString());
                    }
                    // 递归收集 ConfigItem
                    collectComponentKeywords(node, keywords);
                }

                String code = codeContent.toString().isBlank() ? pageEntity.getName() : codeContent.toString();
                String kw = keywords.isEmpty() ? "无" : String.join(", ", keywords);

                String prompt = Prompt.PAGE_SUMMARIZE_PROMPT
                        .replace("{code}", truncate(code))
                        .replace("{keywords}", kw);

                String response = llmService.chat("You are a front-end analysis expert.", prompt);
                String desc = parseFunctionality(response);

                pageEntity.setDescription(desc);
                pageRepo.save(pageEntity);
                funcGenerate.genPageFunc(pageEntity);

                addToMap(pageEntity.getFullName(), pageEntity.getName(), desc, "Page");
                count++;
                log.info("  Page [{}/{}] {} → {}", count, entities.size(), pageEntity.getName(), desc);
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Failed to generate description for Page: {}", pageEntity.getFullName(), e);
            }
        }
        log.info("Page 完成: {}/{}", count, entities.size());
    }

    private void collectComponentKeywords(Node componentNode, Set<String> keywords) {
        try {
            long nodeId = componentNode.get("id") != null
                    ? ((Number) componentNode.get("id")).longValue()
                    : -1;
            if (nodeId < 0) return;
            String cypher = MessageFormat.format("""
                    MATCH (c:Component)-[:CONTAIN]->(ci:ConfigItem)
                    WHERE id(c)={0}
                    RETURN ci
                    """, String.valueOf(nodeId));

            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(cypher);
            while (result.hasNext()) {
                Record record = result.next();
                Node ciNode = record.get("ci").asNode();
                Map<String, Object> props = ciNode.asMap();
                Object label = props.get("label");
                Object annotation = props.get("annotation");
                if (label != null && !label.toString().isBlank()) {
                    keywords.add(label.toString());
                }
                if (annotation != null && !annotation.toString().isBlank()) {
                    keywords.add(annotation.toString());
                }
            }
        } catch (Exception e) {
            // non-critical, just skip keywords for this component
        }
    }

    private String parseFunctionality(String llmResponse) {
        try {
            // Extract JSON from markdown code block if present
            String jsonStr = llmResponse;
            if (llmResponse.contains("```json")) {
                jsonStr = llmResponse.substring(
                        llmResponse.indexOf("```json") + 7,
                        llmResponse.lastIndexOf("```"));
            } else if (llmResponse.contains("```")) {
                jsonStr = llmResponse.substring(
                        llmResponse.indexOf("```") + 3,
                        llmResponse.lastIndexOf("```"));
            }
            JSONObject obj = JSON.parseObject(jsonStr.trim());
            String functionality = obj.getString("functionality");
            return functionality != null ? functionality : llmResponse.trim();
        } catch (Exception e) {
            return llmResponse.trim();
        }
    }

    private String truncate(String code) {
        if (code.length() <= MAX_CODE_LENGTH) {
            return code;
        }
        return code.substring(0, MAX_CODE_LENGTH) + "\n... (truncated)";
    }

    private void addToMap(String fullName, String name, String description, String type) {
        JSONObject entry = new JSONObject();
        entry.put("name", name);
        entry.put("description", description);
        entry.put("type", type);
        descriptionMap.put(fullName, entry);
    }

    private void writeJsonFile() {
        try (FileWriter writer = new FileWriter(OUTPUT_PATH)) {
            writer.write(JSON.toJSONString(descriptionMap, true));
            log.info("描述文件已写入: {}", OUTPUT_PATH);
        } catch (IOException e) {
            log.error("Failed to write descriptions.json", e);
        }
    }
}
