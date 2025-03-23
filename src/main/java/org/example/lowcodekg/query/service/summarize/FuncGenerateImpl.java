package org.example.lowcodekg.query.service.summarize;

import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.query.utils.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.ElasticSearchService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/23 15:47
 */
@Service
public class FuncGenerateImpl implements FuncGenerate {

    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private PageRepo pageRepo;

    @Override
    public void genWorkflowFunc(WorkflowEntity workflowEntity) {
        try {
            String prompt = """
                    You are an expert in programming with a thorough understanding of software projects.
                    The content below provides the method calls and data objects involved in implementing a certain function request within a software project.
                    Based on the code provided, please summarize the implemented function and the technological frameworks, third-party libraries, etc., used during the implementation.
                    Your results should address three aspects: "功能概括," "执行逻辑," and "技术特征." Specifically:
                    * **功能概括**: Provide a concise description of the implemented function without involving technical details, keep it as short as possible.
                    * **执行逻辑**: Describe the overall process of code execution, minimizing technical details.
                    * **技术特征**: Mention any technological frameworks, third-party libraries, tools, etc., involved during the code execution.
                    
                    The code content you need to explain is as follows:
                    {codeContent}
                    
                    Please ensure the output is concise and not too lengthy, also in Chinese, while strictly following the JSON format below without including any additional content:
                    ```json
                    {
                        "功能概括": "",
                        "执行逻辑": "",
                        "技术特征": ""
                    }
                    ```
                    """;
            prompt = prompt.replace("{codeContent}", workflowEntity.getContent());
            String result = llmGenerateService.generateAnswer(prompt);
            Pattern p = Pattern.compile("```json\\s*(\\{[.\\d\\w\\s\\n\\D]*\\})\\s*```");
            Matcher m = p.matcher(result);
            if (m.find()) {
                result = m.group(1);
            } else {
                throw new RuntimeException("LLM generate functionality: format error, " + result);
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            workflowEntity.setDescription(jsonObject.toString());
            WorkflowEntity entity = workflowRepo.save(workflowEntity);

            // create es index
            entity.setEmbedding(EmbeddingUtil.embedText(entity.getDescription()));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("genWorkflowFunc error, " + workflowEntity.getName());
        }
    }

    @Override
    public void genPageFunc(PageEntity pageEntity) {
        try {
            String formattedId = String.format("%d", pageEntity.getId());
            StringBuilder codeContent = new StringBuilder();
            Set<String> keywords = new HashSet<>();
            List<Node> nodeList = new ArrayList<>();

            // get component entity of page entity
            String nodeCypher = MessageFormat.format("""
                MATCH (p:PageTemplate)-[:CONTAIN]->(c:Component)
                where id(p)={0}
                RETURN c
                """, formattedId);
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node node = result.next().get("c").asNode();
                nodeList.add(node);
                // code content
                codeContent.append(node.asMap().get("content"));
                // textual literals
                keywords.add(node.asMap().get("text") + "\n");
            }

            // scan config item property of component entity
            nodeList.forEach(node -> {
                scanComponentProperty(node, keywords);
            });

            // construct final prompt
            String prompt = """
                    以下给定一个前端Vue页面的代码内容，请你为其生成简短的功能概括描述，字数不超过100字。
                    {code}
                    
                    请重点关注以下关键词内容作为生成功能描述的参考。
                    {keywords}                    
                    """;
            prompt = prompt.replace("{code}", codeContent.toString());
            prompt = prompt.replace("{keywords}", keywords.toString());
            String description = llmGenerateService.generateAnswer(prompt);

//            System.out.println(prompt);
//            System.out.println(description);
            // save modification to description of pageEntity
            pageEntity.setDescription(description);
            PageEntity entity = pageRepo.save(pageEntity);

            // create es index
            entity.setEmbedding(EmbeddingUtil.embedText(entity.getDescription()));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("generate PageFunctionality error, " + pageEntity.getFullName());
        }
    }

    /**
     * 递归扫描组件的配置项属性值，判断是否添加到关键词列表中
     */
    private void scanComponentProperty(Node node, Set<String> keywords) {
        try {
            // current component entity contained config item
            String formattedId = String.format("%d", node.id());
            String nodeCypher = MessageFormat.format("""
                MATCH (p:Component)-[:CONTAIN]->(c:ConfigItem)
                where id(p)={0}
                RETURN c
                """, formattedId);
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node n = result.next().get("c").asNode();
                keywords.add(getConfigItemValue(n));
            }
            // child component
            nodeCypher = MessageFormat.format("""
                MATCH (p:Component)-[:PARENT_OF]->(c:Component)
                where id(p)={0}
                RETURN c
                """, formattedId);
            result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node n = result.next().get("c").asNode();
                scanComponentProperty(n, keywords);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("scanConfigItemProperty error, " + node.asMap().get("fullName"));
        }
    }

    private String getConfigItemValue(Node node) {
        try {
            String name = node.asMap().get("name").toString();
            String value = node.asMap().get("value").toString();
            if(name.contains("label") || name.contains("@")) {
                return value;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
}
