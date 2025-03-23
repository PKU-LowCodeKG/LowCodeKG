package org.example.lowcodekg.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.service.FunctionalityGenService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    ```json
                    [
                        {
                            "module": "",
                            "description": "",
                            "workflowList": [] // 列表包含该模块下工作流的序号
                        }
                    ]
                    ```
                    """;
            String res = llmGenerateService.generateAnswer(prompt);
            System.out.println(res);
            Pattern p = Pattern.compile("```json\\s*(\\[[.\\d\\w\\s\\n\\D]*\\])\\s*```");
            Matcher m = p.matcher(res);
            if (m.find()) {
                res = m.group(1);
            } else {
                throw new RuntimeException("generate workflow module format error, " + res);
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


    public static void main(String[] args) {
        String str = """
                ```json
                                    {
                                        "功能概括": "",
                                        "执行逻辑": "",
                                        "技术特征": ""
                                    }
                                    ```
                """;

        Pattern p = Pattern.compile("```json\\s*(\\{[.\\d\\w\\s\\n\\D]*\\})\\s*```");
        Matcher m = p.matcher(str);
        while (m.find()) {
            System.out.println(m.group(1));
        }
    }
}
