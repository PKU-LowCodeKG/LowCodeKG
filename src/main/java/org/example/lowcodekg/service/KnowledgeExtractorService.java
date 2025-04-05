package org.example.lowcodekg.service;

import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.extraction.ExtractorConfig;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.model.dao.neo4j.repository.*;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.summarize.FuncGenerate;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeExtractorService {

    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private ScriptRepo scriptRepo;
    @Autowired
    private ScriptMethodRepo scriptMethodRepo;
    @Autowired
    private ScriptDataRepo scriptDataRepo;
    @Autowired
    private ComponentRepo componentRepo;
    @Autowired
    private ConfigItemRepo configItemRepo;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;
    @Autowired
    private JavaFieldRepo javaFieldRepo;

    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private FuncGenerate funcGenerateService;

    public void execute(String yamlStr)
    {
        initExtractorBean();
//        KnowledgeExtractor.executeFromYaml(yamlStr);
        Yaml yaml = new Yaml();
        Map<String, Object> ret = yaml.load(yamlStr);
        boolean increment = false;
        if (ret.containsKey("increment") && (boolean)ret.get("increment")){
            increment = true;
            ret.remove("increment");
        }
        List<ExtractorConfig> configs = new ArrayList<>();
        for (String key : ret.keySet()) {
            Object value = ret.get(key);
            if(value instanceof String) {
                configs.add(new ExtractorConfig(key, Collections.singletonList((String) value)));
            } else if(value instanceof List) {
                configs.add(new ExtractorConfig(key, (List<String>) ret.get(key)));
            }
        }
        if (!increment){
            String nodeCypher = "MATCH (n) DETACH DELETE n";
            QueryRunner runner = neo4jClient.getQueryRunner();
            runner.run(nodeCypher);

            // 重建ES索引
            elasticSearchService.deleteAllIndices();
            elasticSearchService.createDefaultIndex();
        }
        KnowledgeExtractor.execute(configs);
    }

    private void initExtractorBean() {
        KnowledgeExtractor.setPageRepo(pageRepo);
        KnowledgeExtractor.setScriptRepo(scriptRepo);
        KnowledgeExtractor.setScriptMethodRepo(scriptMethodRepo);
        KnowledgeExtractor.setScriptDataRepo(scriptDataRepo);
        KnowledgeExtractor.setComponentRepo(componentRepo);
        KnowledgeExtractor.setConfigItemRepo(configItemRepo);
        KnowledgeExtractor.setWorkflowRepo(workflowRepo);
        KnowledgeExtractor.setJavaClassRepo(javaClassRepo);
        KnowledgeExtractor.setJavaMethodRepo(javaMethodRepo);
        KnowledgeExtractor.setJavaFieldRepo(javaFieldRepo);

        KnowledgeExtractor.setElasticSearchService(elasticSearchService);
        KnowledgeExtractor.setLlmGenerateService(llmGenerateService);
        KnowledgeExtractor.setNeo4jClient(neo4jClient);
        KnowledgeExtractor.setFuncGenerateService(funcGenerateService);
    }

    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        try {
            Map<String, Object> ret = yaml.load(FileUtils.readFileToString(new File("/Users/chang/Documents/projects/config.yml"), "utf-8"));
            for (String key : ret.keySet()) {
                Object value = ret.get(key);
                if(value instanceof String) {
                    System.out.println((String) value);
                } else if(value instanceof List) {
                    System.out.println((List<String>) value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
