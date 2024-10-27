package org.example.lowcodekg.service;

import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.ConfigItemRepo;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeExtractorService {

    @Autowired
    private ComponentRepo componentRepo;
    @Autowired
    private ConfigItemRepo configItemRepo;

    public void execute(String yamlStr)
    {
        KnowledgeExtractor.setComponentRepo(componentRepo);
        KnowledgeExtractor.setConfigItemRepo(configItemRepo);

        KnowledgeExtractor.executeFromYaml(yamlStr);
    }
}
