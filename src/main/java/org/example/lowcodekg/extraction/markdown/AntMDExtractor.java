package org.example.lowcodekg.extraction.markdown;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.example.lowcodekg.extraction.KnowledgeExtractor;

import lombok.Getter;
import lombok.Setter;

/**
 * Markdown文件解析 AntDesign专用
 */
public class AntMDExtractor extends KnowledgeExtractor {

    @Override
    public void extraction() {
        ArrayList<RawData> dataList = new ArrayList<RawData>();

        // test
        final String path = "E:\\test\\ant-design-master\\components";
        final String[] components = { "button", "affix", "alert" };
        for (int i = 0; i < components.length; i++) {
            RawData data = new RawData();
            parseComponent(path + "/" + components[i] + "/index.zh-CN.md", data);
            dataList.add(data);
            // test
            data.Test();
        }

        // TODO: change rawDataList to Component List

    }

    private void parseComponent(String fileName, RawData data) {
        try (FileInputStream fis = new FileInputStream(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis, "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line, data, br);
            }
            // TODO: Test-data.printInfo();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
    
    private void parseLine(String line, RawData data, BufferedReader br) {
        if (line.equals("---"))
            return;
        if (line.startsWith("category: ")) {
            // category / default = Components
            line.substring(10);
            return;
        }
        ;
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
            parseUsage(data, br);
            return;
        }
        if (line.startsWith("## 代码演示")) {
            // ## 代码演示 / 代码样例
            // parseDemo(data, br);
            return;
        }
        if (line.startsWith("## API")) {
            // ## API / 组件配置项
            parseAPI(data, br);
        }
    }

    private void parseUsage(RawData data, BufferedReader br) {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("##")) {
                    data.setUsage(data.getUsage() + line);
                } else {
                    parseLine(line, data, br);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void parseAPI(RawData data, BufferedReader br) {
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

    public void Test() {
        System.out.println("Hello World!");
        System.out.println("name = " + name);
        System.out.println("name_CN = " + name_CN);
        System.out.println("category = " + category);
        System.out.println("sceneLabel = " + sceneLabel);
        System.out.println("description = " + description);
        System.out.println("usage = " + usage);
    }

}

