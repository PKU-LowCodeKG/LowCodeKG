package org.example.lowcodekg.controller;

import org.example.lowcodekg.service.ElasticSearchService;
import org.example.lowcodekg.schema.entity.workflow.JavaClass;
import org.example.lowcodekg.schema.entity.workflow.JavaMethod;
import org.example.lowcodekg.schema.entity.workflow.JavaField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/elasticsearch")
public class ElasticSearchController {

    @Autowired
    private ElasticSearchService elasticsearchService;

    // 存储JavaClass的嵌入数据
    @PostMapping("/store-java-class-embedding")
    public void storeJavaClassEmbedding(@RequestBody JavaClass javaClass) throws IOException {
        elasticsearchService.storeJavaClassEmbedding(javaClass);
    }

    // 存储JavaMethod的嵌入数据
    @PostMapping("/store-java-method-embedding")
    public void storeJavaMethodEmbedding(@RequestBody JavaMethod javaMethod) throws IOException {
        elasticsearchService.storeJavaMethodEmbedding(javaMethod);
    }

    // 存储JavaField的嵌入数据
    @PostMapping("/store-java-field-embedding")
    public void storeJavaFieldEmbedding(@RequestBody JavaField javaField) throws IOException {
        elasticsearchService.storeJavaFieldEmbedding(javaField);
    }

    // 查询嵌入数据
    @GetMapping("/search-embedding")
    public List<String> searchEmbedding(@RequestParam String query) throws IOException {
        return elasticsearchService.searchEmbedding(query);
    }
}