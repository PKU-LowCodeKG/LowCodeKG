package org.example.lowcodekg.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.AcknowledgedResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/23 14:08
 */
@Service
public class ElasticSearchService {

    private final ElasticsearchClient client;
    private static final String INDEX_NAME = "documents";

    @Autowired
    public ElasticSearchService(ElasticsearchClient client) {
        this.client = client;
    }

    public void deleteIndex() {
        try {
            client.indices().delete(d -> d.index(ElasticSearchService.INDEX_NAME));
            System.out.println("Existing index deleted successfully.");
        } catch (Exception e) {
            // 忽略删除不存在的索引的异常
            System.out.println("Index does not exist, skipping deletion.");
        }
    }

    /**
     * 创建索引
     * @return 创建结果信息
     */
    public String createIndex(Class<Document> documentClass) throws IOException {
        // 首先检查索引是否存在
        boolean indexExists = client.indices().exists(e -> e
                .index(INDEX_NAME)
        ).value();

        // 如果索引已存在，返回提示信息
        if (indexExists) {
            return "索引 '" + INDEX_NAME + "' 已存在，无需重复创建";
        }

        // 动态生成索引映射
        Map<String, Object> properties = new HashMap<>();
        for (Field field : documentClass.getDeclaredFields()) {
            Map<String, Object> fieldMapping = new HashMap<>();
            if (field.getType() == String.class) {
                fieldMapping.put("type", "text");
                fieldMapping.put("analyzer", "standard");
            } else if (field.getType() == float[].class) {
                fieldMapping.put("type", "dense_vector");
                fieldMapping.put("dims", 384); // 假设维度固定为384
                fieldMapping.put("index", true);
                fieldMapping.put("similarity", "cosine");
            }
            // 其他类型的字段可以在这里添加
            properties.put(field.getName(), fieldMapping);
        }

        Map<String, Object> mappings = new HashMap<>();
        mappings.put("properties", properties);

        Map<String, Object> indexMapping = new HashMap<>();
        indexMapping.put("mappings", mappings);

        try {
            // 创建索引
            boolean acknowledged = client.indices().create(c -> {
                        try {
                            return c
                                    .index(INDEX_NAME)
                                    .withJson(new StringReader(new ObjectMapper().writeValueAsString(indexMapping)));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).acknowledged();

            // 返回创建结果
            return acknowledged
                    ? "索引 '" + INDEX_NAME + "' 创建成功"
                    : "索引 '" + INDEX_NAME + "' 创建失败";

        } catch (Exception e) {
            // 捕获可能的异常并返回错误信息
            return "创建索引时发生错误";
        }
    }

    public void indexDocument(Document document) throws IOException {
        client.index(i -> i
                .index(INDEX_NAME)
                .id(document.getId())
                .document(document)
        );
    }

    /**
     * 基于文本内容搜索文档
     * @param query 查询文本
     * @param maxResults 最大返回结果数，默认为10
     * @param minScore 最小相似度阈值，默认为0
     * @return 匹配的文档列表
     */
    public List<Document> searchByText(String query, int maxResults, double minScore) throws IOException {
        SearchResponse<Document> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("name", "content")
                                        .query(query)
                                )
                        )
                        .size(maxResults),
                Document.class
        );

        return extractHits(response, minScore);
    }

    /**
     * 基于文本内容搜索文档（使用默认参数）
     */
    public List<Document> searchByText(String query) throws IOException {
        return searchByText(query, 10, 0.0);
    }

    /**
     * 基于向量搜索文档
     * @param queryVector 查询向量
     * @param maxResults 最大返回结果数，默认为10
     * @param minScore 最小相似度阈值，默认为0
     * @return 匹配的文档列表
     */
    public List<Document> searchByVector(float[] queryVector, int maxResults, double minScore) throws IOException {
        SearchResponse<Document> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .scriptScore(ss -> ss
                                        .query(qq -> qq.matchAll(m -> m))
                                        .script(script -> script
                                                .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                                                .params("queryVector", JsonData.of(queryVector))
                                        )
                                )
                        )
                        .size(maxResults),
                Document.class
        );

        return extractHits(response, minScore);
    }

    /**
     * 基于向量搜索文档（使用默认参数）
     */
    public List<Document> searchByVector(float[] queryVector) throws IOException {
        return searchByVector(queryVector, 10, 0.0);
    }

    /**
     * 从搜索响应中提取命中结果
     * @param response 搜索响应
     * @param minScore 最小相似度阈值
     * @return 文档列表
     */
    private List<Document> extractHits(SearchResponse<Document> response, double minScore) {
        return response.hits().hits().stream()
                .filter(hit -> hit.score() >= minScore)
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}
