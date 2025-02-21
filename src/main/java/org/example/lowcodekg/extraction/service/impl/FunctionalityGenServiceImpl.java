package org.example.lowcodekg.extraction.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.extraction.service.FunctionalityGenService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class FunctionalityGenServiceImpl implements FunctionalityGenService {

    @Autowired
    private OllamaChatModel ollamaChatModel;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private ComponentRepo componentRepo;

    @Override
    public  void genWorkflowModule() {
        try {
            String prompt = """
                    以下给出一个软件项目中多个请求-响应的执行工作流的描述信息，请你根据给出的功能与技术描述对其进行分类，形成一个树形结构的软件项目功能架构树。
                    """;
            // find all workflow entity
            String cypher = """
                    MATCH (n:Workflow)
                    RETURN n
                    """;
            StringBuilder entityList = new StringBuilder();
            int entityCount = 1;
            Map<Integer, WorkflowEntity> workflowEntityMap = new HashMap<>();
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(cypher);
            // iterate all the initial workflow entities
            while(result.hasNext()) {
                Node node = result.next().get("n").asNode();
                Optional<WorkflowEntity> optional = workflowRepo.findById(node.id());
                if(optional.isPresent()) {
                    WorkflowEntity workflowEntity = optional.get();
                    String description = workflowEntity.getDescription();
                    entityList.append(entityCount + ". " + description + "\n");
                    workflowEntityMap.put(entityCount, workflowEntity);
                    entityCount++;
                }
            }
            prompt += entityList.toString();
            prompt += """
                    请严格按照以下json格式返回结果
                    [
                        {
                            "module": "",
                            "description": "",
                            "workflowList": [] // 列表包含该模块下工作流的序号
                        }
                    ]
                    """;
            String res = llmGenerateService.generateAnswer(prompt);
            System.out.println(res);
            if(res.startsWith("```json")) {
                res = res.substring(8, res.length() - 3);
            }
            JSONArray jsonArray = JSON.parseArray(res);
            for(int i = 0; i < jsonArray.size(); i++) {
                WorkflowEntity moduleEntity = new WorkflowEntity();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String module = jsonObject.getString("module");
                String description = jsonObject.getString("description");
                moduleEntity.setName(module);
                moduleEntity.setDescription(description);
                moduleEntity = workflowRepo.save(moduleEntity);
                workflowRepo.setWorkflowModule(moduleEntity.getId());
                JSONArray workflowList = jsonObject.getJSONArray("workflowList");
                for(int j = 0; j < workflowList.size(); j++) {
                    int workflowId = (int) workflowList.get(j);
                    WorkflowEntity workflowEntity = workflowEntityMap.get(workflowId);
                    if(!Objects.isNull(workflowEntity)) {
                        workflowRepo.createRelationOfContainedWorkflow(moduleEntity.getId(), workflowEntity.getId());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("genWorkflowModule error");
        }
    }

    @Override
    public void genWorkflowFunc(WorkflowEntity workflowEntity) {
        try {
            String prompt = """
                    ● 以下给出一个软件项目中实现某一功能请求所涉及的调用方法和操作的数据对象，请你根据代码内容概括实现的功能，以及在实现过程中采取的技术框架、三方库等技术信息，按照“功能概况”、“执行逻辑”和“技术特征”三方面返回结果，其中，
                      ○ 功能概况：对代码实现的功能进行简明扼要的描述，不涉及技术细节，内容尽可能简短
                      ○ 执行逻辑：对代码整体执行的过程进行描述，尽可能不涉及技术细节
                      ○ 技术特征：在代码执行过程中涉及到哪些技术框架、三方库、三方工具等
                    ● 请保证结果简明扼要，内容不要太长，同时务必按照以下的json格式进行输出，不要包含其他内容
                    {
                        "功能概括": "",
                        "执行逻辑": "",
                        "技术特征": ""
                    }\n
                    """ + workflowEntity.getContent();
            String result = llmGenerateService.generateAnswer(prompt);
            if(result.startsWith("```json")) {
                result = result.substring(8, result.length() - 3);
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            workflowEntity.setDescription(jsonObject.toString());
            workflowRepo.save(workflowEntity);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("genWorkflowFunc error, " + workflowEntity.getName());
        }
    }

    @Override
    public void generatePageFunctionality(PageEntity pageEntity) {
        try {
            String formattedId = String.format("%d", pageEntity.getId());
            String prompt = """
                    以下给定一个前端Vue页面的代码内容，请你为其生成简短的功能概括描述，字数不超过100字。
                    {codeContent}
                    
                    请重点关注以下关键词内容作为生成功能描述的参考。
                    {keywords}                    
                    """;
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
            prompt = prompt.replace("{codeContent}", codeContent.toString());
            prompt = prompt.replace("{keywords}", keywords.toString());
            String description = llmGenerateService.generateAnswer(prompt);

//            System.out.println(prompt);
//            System.out.println(description);
            // save modification to description of pageEntity
            pageEntity.setDescription(description);
            pageRepo.save(pageEntity);
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
