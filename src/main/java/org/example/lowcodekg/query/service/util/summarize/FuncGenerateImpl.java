package org.example.lowcodekg.query.service.util.summarize;

import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaMethodRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
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

import static org.example.lowcodekg.query.utils.Constants.*;

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
    private Neo4jClient neo4jClient;
    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private JavaClassRepo classRepo;
    @Autowired
    private JavaMethodRepo methodRepo;

    @Override
    public void genDataObjectFunc(JavaClassEntity classEntity) {
        try {
            String description = classEntity.getDescription() != null
                    ? classEntity.getDescription()
                    : classEntity.getFullName();
            classEntity.setDescription(description);
            JavaClassEntity entity = classRepo.save(classEntity);

            // create es index
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, CODE_ENTITY_INDEX_NAME);
        } catch (Exception e) {
            System.err.println("Error in genDataObjectFunc");
        }
    }
    @Override
    public void genMethodFunc(JavaMethodEntity methodEntity) {
        try {
            String description = methodEntity.getDescription() != null
                    ? methodEntity.getDescription()
                    : methodEntity.getFullName();
            methodEntity.setDescription(description);
            JavaMethodEntity entity = methodRepo.save(methodEntity);

            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, CODE_ENTITY_INDEX_NAME);
        } catch (Exception e) {
            System.err.println("Error in genMethodFunc: " + e.getMessage());
        }
    }
    @Override
    public void genWorkflowFunc(WorkflowEntity workflowEntity) {
        try {
            String description = workflowEntity.getName();
            workflowEntity.setDescription(description);
            WorkflowEntity entity = workflowRepo.save(workflowEntity);

            // create es index
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, CODE_ENTITY_INDEX_NAME);

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

            String description = pageEntity.getName();
            pageEntity.setDescription(description);
            PageEntity entity = pageRepo.save(pageEntity);

            // create es index
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, CODE_ENTITY_INDEX_NAME);
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
