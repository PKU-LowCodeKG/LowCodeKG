package org.example.lowcodekg.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 工具类，操作文件
 */
public class FileUtils {

    /**
     * 读取文件，返回字符串
     */
    public static String readFile(String filename) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

}
