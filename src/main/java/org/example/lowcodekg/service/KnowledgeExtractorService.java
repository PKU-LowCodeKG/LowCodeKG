package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.repository.*;
import org.example.lowcodekg.extraction.ExtractorConfig;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeExtractorService {

    @Autowired
    private ComponentRepo componentRepo;
    @Autowired
    private ConfigItemRepo configItemRepo;
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

    public void execute(String yamlStr)
    {
        KnowledgeExtractor.setComponentRepo(componentRepo);
        KnowledgeExtractor.setConfigItemRepo(configItemRepo);
        KnowledgeExtractor.setJavaClassRepo(javaClassRepo);
        KnowledgeExtractor.setJavaMethodRepo(javaMethodRepo);
        KnowledgeExtractor.setJavaFieldRepo(javaFieldRepo);

        KnowledgeExtractor.setElasticSearchService(elasticSearchService);

//        KnowledgeExtractor.executeFromYaml(yamlStr);
        Yaml yaml = new Yaml();
        Map<String, Object> ret = yaml.load(yamlStr);
        String graphDir = (String) ret.get("graphDir");
        ret.remove("graphDir");
        boolean increment = false;
        if (ret.containsKey("increment") && (boolean)ret.get("increment")){
            increment = true;
            ret.remove("increment");
        }
        List<ExtractorConfig> configs = new ArrayList<>();
        for (String key : ret.keySet()) {
            configs.add(new ExtractorConfig(key, graphDir, (String) ret.get(key)));
        }
        if (new File(graphDir).exists() && !increment){
            String nodeCypher = "MATCH (n) DETACH DELETE n";
            QueryRunner runner = neo4jClient.getQueryRunner();
            runner.run(nodeCypher);
        }
        KnowledgeExtractor.execute(configs);
    }
}
