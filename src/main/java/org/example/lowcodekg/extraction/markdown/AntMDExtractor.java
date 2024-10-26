package org.example.lowcodekg.extraction.markdown;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
import org.example.lowcodekg.schema.entity.Component;
import org.example.lowcodekg.schema.entity.ConfigItem;

import lombok.Getter;
import lombok.Setter;

/**
 * Markdown文件解析 AntDesign专用
 */
public class AntMDExtractor extends KnowledgeExtractor {
    // extract results
    public ArrayList<RawData> dataList = new ArrayList<RawData>();

    // private variables for extracting
    private String WorkDir;     // temp sub-directory for current component
    private final String[] componentNames = { "button", "affix", "alert" };
    private int lineNum;
    
    // Test function
    public static void main(String[] args) {
        System.out.println("Hello world: Test AntMDExtractor.");
        AntMDExtractor test = new AntMDExtractor();
        test.setDataDir("E:\\test\\ant-design-master\\components");
        test.extraction();
        for (int i = 0; i < test.dataList.size(); i++) {
            test.dataList.get(i).Test();
        }
    }

    @Override
    public void extraction() {
        for (int i = 0; i < componentNames.length; i++) {
            RawData data = new RawData();
            WorkDir = this.getDataDir() + "/" + componentNames[i];
            parseComponent(WorkDir + "/index.zh-CN.md", data);
            dataList.add(data);
        }
        return;
    }

    private void parseComponent(String fileName, RawData data) {
        lineNum = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
            while (lineNum < lines.size()) {
                parseLine(lines, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
    
    private void parseLine(List<String> lines, RawData data) {
        String line = lines.get(lineNum++);
        if (line.equals("---")) return;
        if (line.startsWith("category: ")) {
            // category / default = Components
            data.setCategory(line.substring(10));
            line.substring(10);
            return;
        }
        if (line.startsWith("title: ")) {
            // title / name-EN
            data.setName(line.substring(7));
            return;
        }
        if (line.startsWith("subtitle")) {
            // subtitle / name-CN
            data.setName_CN(line.substring(10));
            return;
        }
        if (line.startsWith("description: ")) {
            // description / description-CN-concise
            data.setDescription(line.substring(13));
            return;
        }
        if (line.startsWith("## 何时使用")) {
            // ## 何时使用 / usage
            parseUsage(lines, data);
            return;
        }
        if (line.startsWith("## 代码演示")) {
            // ## 代码演示 / 代码样例
            parseDemo(lines, data);
            return;
        }
        if (line.startsWith("## API")) {
            // ## API / 组件配置项
            parseConfig(lines, data);
        }
    }

    private void parseUsage(List<String> lines, RawData data) {
        String line;
        while (lineNum < lines.size()) {
            line = lines.get(lineNum);
            if (!line.startsWith("##")) {
                data.setUsage(data.getUsage() + line);
            } else {
                parseLine(lines, data);
                return;
            }
            lineNum++;
        }
        return;
    }

    private void parseDemo(List<String> lines, RawData data) {
        String line;
        while (lineNum < lines.size()) {
            line = lines.get(lineNum);
            if (line.startsWith("##")) {
                parseLine(lines, data);
                return;
            }
            if (line.startsWith("<code src=") && line.indexOf(".tsx") >= 18) {
                String demoName = line.substring(18, line.indexOf(".tsx"));
                CodeDemo demo = new CodeDemo(demoName);
                demo.setName_CN(line.substring(line.indexOf(">", line.indexOf(".tsx", 0)) + 1,
                line.indexOf("</code>")));
                // parse demo files (Code & Description)
                demo.parseCode(WorkDir + "/demo/" + demoName + ".tsx");
                demo.parseDescription(WorkDir + "/demo/" + demoName + ".md");
                data.getCodeDemos().add(demo);
            }
            lineNum++;
        }
        return;
    }

    private void parseConfig(List<String> lines, RawData data) {
        // TODO: parseAPI info from .md file
    }
}

class RawData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String name_CN;

    @Getter
    @Setter
    private String category;

    @Getter
    @Setter
    private String sceneLabel;

    @Getter
    @Setter
    private String description = "";

    @Getter
    @Setter
    private String usage = "";

    @Getter
    private ArrayList<CodeDemo> codeDemos = new ArrayList<CodeDemo>();

    // Test function for RawData
    public void Test() {
        System.out.println("Hello World!");
        System.out.println("name = " + name);
        System.out.println("name_CN = " + name_CN);
        System.out.println("category = " + category);
        System.out.println("sceneLabel = " + sceneLabel);
        System.out.println("description = " + description);
        System.out.println("usage = " + usage);
        for (int i = 0; i < codeDemos.size(); i++) {
            codeDemos.get(i).Test();
        }
    }

    public void toComponent(Component component) {
        component.setName(name);
        component.setCategory(ComponentCategory.UI_COMPONENT);
        // TODO: parse category from data info
        component.setSceneLabel(SceneLabel.OTHER);
        component.setDescription(description);
        return;
    }
}

class CodeDemo {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String name_CN;
    
    @Getter
    @Setter
    private String code = "";

    @Getter
    @Setter
    private String description = "";


    public CodeDemo(String name) {
        this.name = name;
    }

    public void parseCode(String fileName) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                code += (lines.get(i) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseDescription(String fileName) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                description += (lines.get(i) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Test function for CodeDemo
    public void Test() {
        System.out.println("name = " + name);
        System.out.println("name_CN = " + name_CN);
        System.out.println("code = " + code);
        System.out.println("description = " + description);
    }

    public void toConfigItem() {
        // TODO: add function
    }
}