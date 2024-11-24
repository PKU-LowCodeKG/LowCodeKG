package org.example.lowcodekg.extraction;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.dao.neo4j.repository.*;
import org.example.lowcodekg.schema.entity.workflow.JavaMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 定义知识挖掘的抽象类
 */
public abstract class KnowledgeExtractor {

    @Setter
    protected static ComponentRepo componentRepo;
    @Setter
    protected static ConfigItemRepo configItemRepo;
    @Setter
    protected static JavaClassRepo javaClassRepo;
    @Setter
    protected static JavaMethodRepo javaMethodRepo;
    @Setter
    protected static JavaFieldRepo javaFieldRepo;

    @Getter
    @Setter
    private String graphDir;

    @Getter
    @Setter
    private String dataDir;

    public static void execute(List<ExtractorConfig> extractorConfigList) {
        for (ExtractorConfig config : extractorConfigList) {
            System.out.println(config.getClassName() + " start ...");
            KnowledgeExtractor extractor = null;
            try {
                extractor = (KnowledgeExtractor) Class.forName(config.getClassName()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            extractor.setGraphDir(config.getGraphDir());
            extractor.setDataDir(config.getDataDir());
            try {
                extractor.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(config.getClassName() + " finished.");
        }
    }

//    public static void main(String[] args) {
//        try {
//            KnowledgeExtractor.executeFromYaml(FileUtils.readFileToString(new File("/Users/chang/Documents/projects/LowCodeKG/config.yml"), "utf-8"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public abstract void extraction();

    public void execute() throws IOException {
        this.extraction();
    }
}
