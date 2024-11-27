package org.example.lowcodekg.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.example.lowcodekg.schema.entity.workflow.JavaClass;
import org.example.lowcodekg.schema.entity.workflow.JavaMethod;
import org.example.lowcodekg.schema.entity.workflow.JavaField;
@Service
public class ElasticSearchService {

    private final RestClient client;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Autowired
    public ElasticSearchService(RestClient client) {
        this.client = client;
        this.embeddingStore = ElasticsearchEmbeddingStore.builder().restClient(client).build();
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }
//    public void storeEmbedding(Object obj) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        String description;
//        String vid;
//
//        if (obj instanceof JavaClass javaClass) {
//            description = javaClass.getDescription();
//            vid = String.valueOf(javaClass.getVid());
//        } else if (obj instanceof JavaMethod javaMethod) {
//            description = javaMethod.getDescription();
//            vid = String.valueOf(javaMethod.getVid());
//        } else if (obj instanceof JavaField javaField) {
//            description = javaField.getDescription();
//            vid = String.valueOf(javaField.getVid());
//        } else {
//            throw new IllegalArgumentException("Unsupported object type: " + obj.getClass().getName());
//        }
//
//        if (description != null && !description.isEmpty()) {
//            TextSegment descriptionSegment = TextSegment.from(description);
//            TextSegment vidSegment = TextSegment.from(vid);
//            Embedding embedding = embeddingModel.embed(descriptionSegment).content();
//            embeddingStore.add(embedding, vidSegment);
//        } else {
//            throw new IllegalArgumentException("Description must be non-null and non-empty");
//        }
//    }
    //
    public void storeJavaClassEmbedding(JavaClass javaClass) {
        if (javaClass.getDescription() != null && !javaClass.getDescription().isEmpty()) {
            TextSegment descriptionSegment = TextSegment.from(javaClass.getDescription());
            TextSegment vidSegment = TextSegment.from(javaClass.getVid());
            Embedding embedding = embeddingModel.embed(descriptionSegment).content();

            embeddingStore.add(embedding, vidSegment);
        } else {
            System.out.println(javaClass.getFullName() + " description is null or empty");
        }
    }

    public void storeJavaMethodEmbedding(JavaMethod javaMethod) throws IOException {
        if (javaMethod.getDescription() != null && !javaMethod.getDescription().isEmpty()) {
            TextSegment descriptionSegment = TextSegment.from(javaMethod.getDescription());
            TextSegment vidSegment = TextSegment.from(String.valueOf(javaMethod.getVid()));
            Embedding embedding = embeddingModel.embed(descriptionSegment).content();

            embeddingStore.add(embedding, vidSegment);
        } else {
            System.out.println(javaMethod.getFullName() + " description is null or empty");
        }
    }
    public void storeJavaFieldEmbedding(JavaField javaField) throws IOException {
        if (javaField.getDescription() != null && !javaField.getDescription().isEmpty()) {
            TextSegment descriptionSegment = TextSegment.from(javaField.getDescription());
            TextSegment vidSegment = TextSegment.from(String.valueOf(javaField.getVid()));
            Embedding embedding = embeddingModel.embed(descriptionSegment).content();

            embeddingStore.add(embedding, vidSegment);
        } else {
            System.out.println(javaField.getFullName() + " description is null or empty");
        }
    }
    // 查询嵌入数据
    public List<String> searchEmbedding(String query) throws IOException {
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
}