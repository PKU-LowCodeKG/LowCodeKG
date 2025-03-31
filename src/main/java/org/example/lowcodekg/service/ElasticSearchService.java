package org.example.lowcodekg.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import io.micrometer.common.util.StringUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.example.lowcodekg.model.schema.entity.workflow.JavaClass;
import org.example.lowcodekg.model.schema.entity.workflow.JavaMethod;
import org.example.lowcodekg.model.schema.entity.workflow.JavaField;


@Service
public class ElasticSearchService {

    private final RestClient client;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;


    @Autowired
    public ElasticSearchService(RestClient client) {
        this.client = client;
        this.embeddingStore = ElasticsearchEmbeddingStore.builder().restClient(client).configuration(ElasticsearchConfigurationKnn.builder().build()).build();
        this.embeddingModel = new BgeSmallZhV15QuantizedEmbeddingModel();
    }

    public void storeJavaClassEmbedding(JavaClass javaClass) {
        if (StringUtils.isNotEmpty(javaClass.getDescription())) {
            TextSegment descriptionSegment = TextSegment.from(javaClass.getDescription());
            TextSegment vidSegment = TextSegment.from(String.valueOf(javaClass.getVid()));
            Embedding embedding = embeddingModel.embed(descriptionSegment).content();

            embeddingStore.add(embedding, vidSegment);
        } else {
            System.out.println(javaClass.getFullName() + " description is null or empty");
        }
    }

    public void storeJavaMethodEmbedding(JavaMethod javaMethod) {
        if (StringUtils.isNotEmpty(javaMethod.getDescription())) {
            TextSegment descriptionSegment = TextSegment.from(javaMethod.getDescription());
            TextSegment vidSegment = TextSegment.from(String.valueOf(javaMethod.getVid()));
            Embedding embedding = embeddingModel.embed(descriptionSegment).content();

            embeddingStore.add(embedding, vidSegment);
        } else {
            System.out.println(javaMethod.getFullName() + " description is null or empty");
        }
    }
    public void storeJavaFieldEmbedding(JavaField javaField) {
        if (StringUtils.isNotEmpty(javaField.getDescription())) {
            TextSegment descriptionSegment = TextSegment.from(javaField.getDescription());
            TextSegment vidSegment = TextSegment.from(String.valueOf(javaField.getVid()));
            Embedding embedding = embeddingModel.embed(descriptionSegment).content();

            embeddingStore.add(embedding, vidSegment);
        } else {
//            System.out.println(javaField.getFullName() + " description is null or empty");
        }
    }

    /**
     * 查询嵌入数据
     */
    public List<String> searchEmbedding(String query) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        TextSegment querySegment = TextSegment.from(query);
        Embedding queryEmbedding = embeddingModel.embed(querySegment).content();
        EmbeddingSearchResult<TextSegment> relevant = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(5)
                        .build());
        List<String> fullNames = new ArrayList<>();
        for(EmbeddingMatch<TextSegment> match : relevant.matches()) {
            fullNames.add(match.embedded().text());
        }
        return fullNames;
    }

    public void deleteDefaultIndex() {
        try {
            // 构建 DELETE 请求
            Request request = new Request("DELETE", "/default");
            Response response = client.performRequest(request);

            // 打印响应状态
            System.out.println("ES delete response status: " + response.getStatusLine());
        } catch (IOException e) {
            System.err.println("Error while deleting index data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}