package org.example.lowcodekg.extraction.document;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CodeDemo {
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
}
