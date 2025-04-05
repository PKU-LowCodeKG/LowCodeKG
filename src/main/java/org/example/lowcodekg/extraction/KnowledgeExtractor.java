package org.example.lowcodekg.extraction;

import lombok.Getter;
import lombok.Setter;
import org.example.lowcodekg.model.dao.neo4j.repository.*;
import org.example.lowcodekg.query.service.util.summarize.FuncGenerate;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.IOException;
import java.util.List;

/**
 * 定义知识挖掘的抽象类
 */
public abstract class KnowledgeExtractor {

    @Setter
    protected static PageRepo pageRepo;
    @Setter
    protected static ScriptRepo scriptRepo;
    @Setter
    protected static ScriptMethodRepo scriptMethodRepo;
    @Setter
    protected static ScriptDataRepo scriptDataRepo;
    @Setter
    protected static ComponentRepo componentRepo;
    @Setter
    protected static ConfigItemRepo configItemRepo;
    @Setter
    protected static WorkflowRepo workflowRepo;
    @Setter
    protected static JavaClassRepo javaClassRepo;
    @Setter
    protected static JavaMethodRepo javaMethodRepo;
    @Setter
    protected static JavaFieldRepo javaFieldRepo;

    @Setter
    protected static ElasticSearchService elasticSearchService;
    @Setter
    protected static LLMGenerateService llmGenerateService;
    @Setter
    protected static Neo4jClient neo4jClient;
    @Setter
    protected static FuncGenerate funcGenerateService;

    @Getter
    @Setter
    private List<String> dataDir;

    public static void execute(List<ExtractorConfig> extractorConfigList) {
        for (ExtractorConfig config : extractorConfigList) {
            System.out.println(config.getClassName() + " start ...");
            KnowledgeExtractor extractor = null;
            try {
                extractor = (KnowledgeExtractor) Class.forName(config.getClassName()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            extractor.setDataDir(config.getDataDir());
            try {
                extractor.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(config.getClassName() + " finished.");
        }
    }


    public abstract void extraction();

    public void execute() throws IOException {
        this.extraction();
    }
}
