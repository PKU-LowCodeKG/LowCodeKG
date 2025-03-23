package org.example.lowcodekg.service;

import org.example.lowcodekg.model.dao.es.document.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ElasticSearchServiceTest {

    @Autowired
    private ElasticSearchService esService;

    @BeforeEach
    void setUp() throws IOException {
        // 确保索引存在
        String result = esService.createIndex();
        System.out.println("Index creation result: " + result);
    }

    @Test
    void testCreateIndex() throws IOException {
        // 测试重复创建索引
        String result = esService.createIndex();
        assertTrue(result.contains("已存在"));
    }

    @Test
    void testIndexAndSearchDocument() throws IOException, InterruptedException {
        // 创建测试文档
        Document doc1 = new Document();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setName("Java Programming Guide");
        doc1.setDescription("Java is a popular programming language. It is used for web development, Android apps, and enterprise software.");
        float[] embedding1 = new float[384];
        // 填充一些示例向量值
        for (int i = 0; i < embedding1.length; i++) {
            embedding1[i] = (float) Math.random();
        }
        doc1.setEmbedding(embedding1);

        Document doc2 = new Document();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setName("Python Tutorial");
        doc2.setDescription("Python is an easy to learn programming language. It's great for data science and machine learning.");
        float[] embedding2 = new float[384];
        for (int i = 0; i < embedding2.length; i++) {
            embedding2[i] = (float) Math.random();
        }
        doc2.setEmbedding(embedding2);

        // 索引文档
        esService.indexDocument(doc1);
        esService.indexDocument(doc2);

        // 等待索引刷新
        Thread.sleep(1000);

        // 测试文本搜索（自定义参数）
        List<Document> results2 = esService.searchByText("Java programming", 1, 0);
        System.out.println(results2);

        // 测试向量搜索
        List<Document> results3 = esService.searchByVector(embedding1);
        System.out.println(results3);

        // 测试向量搜索（自定义参数）
        List<Document> results4 = esService.searchByVector(embedding2, 2, 0.7);
        System.out.println(results4);
    }
} 