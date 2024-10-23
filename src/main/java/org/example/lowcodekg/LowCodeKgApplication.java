package org.example.lowcodekg;

import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;

@EnableTransactionManagement
@EnableNeo4jRepositories
@SpringBootApplication
public class LowCodeKgApplication {

    public static void main(String[] args) {

        System.setProperty("file.encoding", "utf-8");
        CmdOption option = new CmdOption();
        CmdLineParser parser = new CmdLineParser(option);

        try {
            if (args.length == 0) {
                return;
            }
            parser.parseArgument(args);
            if (option.exec) {
                SpringApplication.run(LowCodeKgApplication.class, args);
            } else if (option.genConfigPath != null) {
                KnowledgeExtractor.executeFromYaml(FileUtils.readFileToString(new File(option.genConfigPath), "utf-8"));
            }

        } catch (CmdLineException cle) {
            System.out.println("Command line error: " + cle.getMessage());
            return;
        } catch (Exception e) {
            System.out.println("Error in main: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}

class CmdOption {

    @Option(name = "-gen", usage = "Generate a knowledge graph according to the yaml configure file")
    public String genConfigPath = null;

    @Option(name = "-exec", usage = "Run the web application in localhost", handler = ExplicitBooleanOptionHandler.class)
    public boolean exec = false;
}