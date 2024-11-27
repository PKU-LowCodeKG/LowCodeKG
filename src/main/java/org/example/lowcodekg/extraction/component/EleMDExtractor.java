package org.example.lowcodekg.extraction.component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.schema.entity.Component;
import org.springframework.stereotype.Service;

@Service
public class EleMDExtractor extends KnowledgeExtractor {

    // extract results
    public ArrayList<RawData> dataList = new ArrayList<RawData>();

    // private variables for extracting
    private String WorkDir;
    private int lineNum;
    private String subTitle;
    private String WorkComponent;

    @Override
    public void extraction() {
        parseData();

        for (RawData data : dataList) {
            Component component = data.convertToComponent();
            component.storeInNeo4j(componentRepo, configItemRepo);
        }
        return;
    }

    private void parseComponent(File componentFile, RawData data) {
        lineNum = 0;
        try {
            List<String> lines = Files.readAllLines(componentFile.toPath(), StandardCharsets.UTF_8);
            while (lineNum < lines.size()) {
                parseLine(lines, data);
                data.setLanguage("Vue3");
                data.setSource("Element-Plus");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void parseLine(List<String> lines, RawData data) {
        String line = lines.get(lineNum++);
        if (line.startsWith("---"));
        if (line.startsWith("title:")) {
            data.setName(line.substring(7));
            return;
        }
        if (line.startsWith("lang:")) {
            // the doc language; skip.
            return;
        }
        if (line.startsWith("# " + data.getName())) {
            lineNum++;
            line = lines.get(lineNum++);
            data.setDescription(line);
            return;
        }
        if (line.startsWith("## ")) {
            this.subTitle = line.substring(3);
            parseDemo(lines, data);
            return;
        }
        if (line.startsWith("### ")) {
            // API or other tables
            this.subTitle = line.substring(4);
            parseConfig(lines, data);
            return;
        }
    }

    private void parseDemo(List<String> lines, RawData data) {
        String line;
        while (lineNum < lines.size()) {
            line = lines.get(lineNum);
            if (line.startsWith("#")) {
                parseLine(lines, data);
                return;
            }
            if (line.startsWith(":::demo ")) {
                CodeDemo demo = new CodeDemo("");
                if (line.length() > 9) {
                    demo.setDescription(line.substring(8));
                }
                lineNum += 2;
                line = lines.get(lineNum);
                if (line.startsWith(WorkComponent + "/")) {
                    demo.setName(line);
                }

                demo.parseCode(this.getDataDir() + "/docs/examples/" + demo.getName() + ".vue");
                data.getCodeDemos().add(demo);
                // TODO: :::demo前，可能会有若干其他样式的说明。暂未加入。
            }

            lineNum++;
        }
        return;
    }

    private void parseConfig(List<String> lines, RawData data) {
        String line;
        int nameIndex = -1, descIndex = -1, typeIndex = -1, defaultIndex = -1;
        // local variables

        while (lineNum < lines.size()) {
            line = lines.get(lineNum);
            // TODO: 针对不同配置项的子标题作记录
            
            if (line.startsWith("#")) {
                parseLine(lines, data);
                return;
            }
            if (line.startsWith("| Name") || line.startsWith("| Property") || line.startsWith("| Attribute") || line.startsWith("| Method")) {
                nameIndex = (line.indexOf("Name") > 0) ? line.indexOf("Name") : line.indexOf("Property");
                nameIndex = nameIndex > 0 ? nameIndex : line.indexOf("Attribute");
                nameIndex = nameIndex > 0 ? nameIndex : line.indexOf("Method");
                // TODO: “配置项”分了好几种类别，暂时没有做区分
                // 未处理的特殊内容: Variable, name
                descIndex = line.indexOf("Description");
                typeIndex = line.indexOf("Type");
                defaultIndex = line.indexOf("Default");
            }
            if (line.startsWith("| ") && !line.startsWith("| -") && !line.startsWith("| Name") && !line.startsWith("| Property") && !line.startsWith("| Attribute") && !line.startsWith("| Method")) {
                RawConfigItem config = new RawConfigItem();
                if (nameIndex > 0) {
                    config.setName(line.substring(nameIndex, line.indexOf(" |", nameIndex)));
                }
                if (descIndex > 0) {
                    config.setDescription("### " + subTitle + ": " + line.substring(descIndex, line.indexOf((" |"), descIndex)));
                }
                if (typeIndex > 0) {
                    config.setType(line.substring(typeIndex, line.indexOf(" |", typeIndex)));
                }
                if (defaultIndex > 0) {
                    config.setDefaultValue(line.substring(defaultIndex, line.indexOf(" |", defaultIndex)));
                }

                if (nameIndex <= 0) {
                    System.err.println("Error: Config Name Not Found: " + WorkComponent + " " + line);
                }
                else {
                    data.getConfigItems().add(config);
                }
            }

            // end
            lineNum++;
        }
    }

    public void parseData() {
        WorkDir = this.getDataDir() + "/docs/en-US/component";
        File[] componentFiles = new File(this.WorkDir).listFiles();
        for (int i = 0; i < componentFiles.length; i++) {
            if (componentFiles[i].getName().endsWith(".md")) {
                RawData data = new RawData();
                WorkComponent = componentFiles[i].getName();
                WorkComponent = WorkComponent.substring(0, WorkComponent.length() - 3);
                parseComponent(componentFiles[i], data);
                dataList.add(data);
            }
        }
        return;
    }

}
