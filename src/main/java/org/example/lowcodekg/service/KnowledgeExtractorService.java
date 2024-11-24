package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.repository.*;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void execute(String yamlStr)
    {
        KnowledgeExtractor.setComponentRepo(componentRepo);
        KnowledgeExtractor.setConfigItemRepo(configItemRepo);
        KnowledgeExtractor.setJavaClassRepo(javaClassRepo);
        KnowledgeExtractor.setJavaMethodRepo(javaMethodRepo);
        KnowledgeExtractor.setJavaFieldRepo(javaFieldRepo);

        KnowledgeExtractor.executeFromYaml(yamlStr);
    }
}
