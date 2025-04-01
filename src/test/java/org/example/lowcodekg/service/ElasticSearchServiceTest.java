package org.example.lowcodekg.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.service.retriever.ElasticSearchService;
import org.example.lowcodekg.query.utils.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.lowcodekg.query.utils.Constants.*;
import static org.example.lowcodekg.query.utils.Prompt.TYPE_OF_RETRIEVED_ENTITY_PROMPT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ElasticSearchServiceTest {

    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void setUp() throws IOException {
        esService.deleteIndex("test");
        // 确保索引存在
        String result = esService.createIndex(Document.class, "test");
        System.out.println("Index creation result: " + result);
    }

    @Test
    void testCreateIndex() throws IOException {
        // 测试重复创建索引
        String result = esService.createIndex(Document.class, "test");
        assertTrue(result.contains("已存在"));
    }

    @Test
    void testIndexAndSearchDocument() throws IOException, InterruptedException {
        // 创建测试文档
        Document doc1 = new Document();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setName("Java Programming Guide");
        doc1.setContent("Java is a popular programming language. It is used for web development, Android apps, and enterprise software.");
        doc1.setNeo4jId(1L);
        float[] embedding1 = FormatUtil.ListToArray(EmbeddingUtil.embedText(doc1.getName() + "\n" + doc1.getContent()));
        doc1.setEmbedding(embedding1);

        Document doc2 = new Document();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setName("Python Tutorial");
        doc2.setContent("Python is an easy to learn programming language. It's great for data science and machine learning.");
        float[] embedding2 = FormatUtil.ListToArray(EmbeddingUtil.embedText(doc2.getName() + "\n" + doc2.getContent()));
        doc2.setEmbedding(embedding2);

        // 索引文档
        esService.indexDocument(doc1, "test");
        esService.indexDocument(doc2, "test");

        // 等待索引刷新
        Thread.sleep(1000);

        // 测试文本搜索（自定义参数）
//        List<Document> results2 = esService.searchByText("Java programming", 1, 0, "test");
//        System.out.println(results2.get(0).getNeo4jId());

        // 测试向量搜索
        float[] queryVector = FormatUtil.ListToArray(EmbeddingUtil.embedText("Java programming"));
        List<Document> results3 = esService.searchByVector(queryVector, 1, 0.65, "test");
        System.out.println(results3);
    }

    @Test
    public void testCalculateSimilarity() {
        String text1 = "实现用户注册流程，包括邮箱验证和手机号验证";
        String text2 = "我要实现一个用户通过邮箱注册的功能";
        String text3 = "我要开发一个允许用户取消收藏夹商品的功能";

        List<Float> embedding1 = EmbeddingUtil.embedText(text1);
        List<Float> embedding2 = EmbeddingUtil.embedText(text2);
        List<Float> embedding3 = EmbeddingUtil.embedText(text3);
        System.out.println(embedding1.size());
        double sim1 = EmbeddingUtil.calculateSimilarity(text1, text2);
        double sim2 = EmbeddingUtil.calculateSimilarity(text1, text3);
        double sim3 = EmbeddingUtil.calculateSimilarity(text2, text3);
        System.out.println(sim1);
        System.out.println(sim2);
        System.out.println(sim3);
    }

    @Test
    void testSubTaskRetrieval() throws IOException {
        String taskInfo = """
                
                """;
        String prompt = TYPE_OF_RETRIEVED_ENTITY_PROMPT.replace("{Task}", taskInfo);
        String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
        System.out.println("retrieval category: " + answer);
        JSONObject jsonObject = JSON.parseObject(answer);

        // 基于ES向量检索，获取候选列表
        List<Document> documents = new ArrayList<>();
        float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(taskInfo));
        for(String key : jsonObject.keySet()) {
            if("Page".equals(key)) {
                Boolean isPage = jsonObject.getBoolean(key);
                if(BooleanUtils.isTrue(isPage)) {
                    documents.addAll(esService.searchByVector(
                            vector, MAX_RESULTS, MIN_SCORE, PAGE_INDEX_NAME
                    ));
                }
            } else if("Workflow".equals(key)) {
                Boolean isWorkflow = jsonObject.getBoolean(key);
                if(BooleanUtils.isTrue(isWorkflow)) {
                    documents.addAll(esService.searchByVector(
                            vector, MAX_RESULTS, MIN_SCORE, WORKFLOW_INDEX_NAME
                    ));
                }
            } else if("DataObject".equals(key)) {
                Boolean isDataObject = jsonObject.getBoolean(key);
                if(BooleanUtils.isTrue(isDataObject)) {
                    documents.addAll(esService.searchByVector(
                            vector, MAX_RESULTS, MIN_SCORE, DATA_OBJECT_INDEX_NAME
                    ));
                }
            }
        }
        List<Node> nodeList = documents.stream()
                .map(this::convertToNeo4jNode)
                .collect(Collectors.toList());
        for(Node node : nodeList) {
            System.out.println(node.toString());
        }
    }

    private Node convertToNeo4jNode(Document document) {
        // 根据 Document 的属性创建 Neo4jNode
        Node node = new Node();
        Long id = Long.valueOf(document.getId());
        String cypher = "MATCH (n) WHERE ID(n) = " + id + " RETURN n";
        QueryRunner runner = neo4jClient.getQueryRunner();
        org.neo4j.driver.Result result = runner.run(cypher);
        if(result.hasNext()) {
            org.neo4j.driver.types.Node n = result.next().get("n").asNode();

            node.setName(n.get("name").asString());
            node.setContent(n.get("content").asString());
            node.setDescription(n.get("description").asString());
        } else {
            return null;
        }
        return node;
    }
} 