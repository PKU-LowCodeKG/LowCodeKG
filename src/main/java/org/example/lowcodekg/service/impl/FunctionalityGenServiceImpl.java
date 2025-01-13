package org.example.lowcodekg.service.impl;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.dao.neo4j.entity.page.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.service.FunctionalityGenService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Override
    public void generatePageFunctionality(PageEntity pageEntity) {
        try {
            String prompt = """
                    以下给定一个前端Vue页面的代码内容，请你为其生成简短的功能描述，字数不超过100字。
                    {codeContent}
                    
                    请重点关注以下关键词内容作为生成功能描述的参考。
                    {keywords}                    
                    """;
            StringBuilder codeContent = new StringBuilder();
            StringBuilder keywords = new StringBuilder();

            String nodeCypher = MessageFormat.format("""
                MATCH (p:PageTemplate)-[:CONTAIN]->(c:Component)
                where id(p)={0}
                RETURN c
                """, pageEntity.getId());
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node node = result.next().get("c").asNode();
                codeContent.append(node.asMap().get("content") + "\n");
                keywords.append(node.asMap().get("text") + "\n");
            }

            prompt = prompt.replace("{codeContent}", codeContent.toString());
            prompt = prompt.replace("{keywords}", keywords.toString());
            String description = llmGenerateService.generateAnswer(prompt);

            System.out.println(prompt);
            System.out.println(description);
            pageEntity.setDescription(description);
            pageRepo.save(pageEntity);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("generate PageFunctionality error, " + pageEntity.getFullName());
        }
    }

}
