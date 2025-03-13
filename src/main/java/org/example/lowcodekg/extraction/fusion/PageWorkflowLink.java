package org.example.lowcodekg.extraction.fusion;

import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description 关联前端页面与后端工作流实体
 * @Author Sherloque
 * @Date 2025/3/7 16:13
 */
@Service
public class PageWorkflowLink extends KnowledgeExtractor {

    private final Map<String, Map<String, Object>> patterns = new HashMap<>();
    List<Map<String, String>> report = new ArrayList<>();

    @Override
    public void extraction() {
        try {
            // 前端匹配模式
            Map<String, Object> frontendPattern = new HashMap<>();
            frontendPattern.put("regex", "(axios|this\\.axios|fetch)\\.(get|post|put|delete)\\s*\\(\\s*['\"`]([^'\"`]*)");
            frontendPattern.put("extensions", Arrays.asList("*.vue", "*.js"));
            report.clear();

            // 遍历前端目录，并使用正则表达式匹配接口请求，并关联到Neo4j实体
            for(String frontEndPath: this.getDataDir()) {
                Path frontPath = Paths.get(frontEndPath);
                List<String> frontendExtensions = (List<String>) frontendPattern.get("extensions");
                for(String ext: frontendExtensions) {
                    // 使用通配符查找文件
                    List<String> files = findFiles(frontPath.toString(), ext);
                    for (String filepath : files) {
                        processFrontendFile(
                                filepath,
                                frontEndPath,
                                (String) frontendPattern.get("regex")
                        );
                    }
                }
            }

            // 从Neo4j数据库查询Workflow实体
            processBackendFile();

            // 关联前端页面与后端工作流实体


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in Page-Workflow linking: " + e.getMessage());
        }
    }

    private void processFrontendFile(String filepath, String frontPath, String pattern) {
        try {
            // 读取文件内容
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filepath, java.nio.charset.Charset.forName("UTF-8")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            // 获取相对路径
            Path relativePath = Paths.get(frontPath).relativize(Paths.get(filepath));

            // 创建正则表达式匹配器
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(content.toString());

            // 查找所有匹配项，并匹配Neo4j数据库中节点
            while (matcher.find()) {
                String lib = matcher.group(1);      // axios/this.axios/fetch
                String method = matcher.group(2);   // HTTP方法
                String url = matcher.group(3);      // URL

                // 创建报告条目
                Map<String, String> entry = Map.of(
                        "type", "Frontend API Call",
                        "file", relativePath.toString(),
                        "method", method.toUpperCase(),
                        "url", url,
                        "snippet", truncateSnippet(matcher.group(0), 100)
                );

                report.add(entry);
            }
        } catch (Exception e) {
            System.err.printf("Error processing %s: %s%n", filepath, e.getMessage());
        }
    }

    private void processBackendFile() {

    }

    /**
     * 根据最大长度截断代码片段
     * 如果代码片段的长度超过最大长度限制，则截断并添加省略标记，否则原样返回
     *
     * @param snippet 原始代码片段
     * @param maxLength 最大允许长度
     * @return 截断后的代码片段或原代码片段
     */
    private String truncateSnippet(String snippet, int maxLength) {
        return snippet.length() > maxLength ? snippet.substring(0, maxLength) + "..." : snippet;
    }

    /**
     * 在指定目录中查找符合特定模式的文件
     * 此方法使用递归方式遍历目录，并通过正则表达式匹配文件名
     *
     * @param directory 要查找的目录路径
     * @param pattern 文件名的通配符模式，"."匹配任何单个字符，"*"匹配零个或多个字符
     * @return 匹配指定模式的文件列表
     */
    private List<String> findFiles(String directory, String pattern) {
        List<String> files = new ArrayList<>();
        try {
            // 将通配符模式转换为正则表达式
            String regex = pattern.replace(".", "\\.")
                    .replace("*", ".*");

            // 递归遍历目录
            Files.walk(Paths.get(directory))
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(path -> path.matches(".*" + regex))
                    .forEach(files::add);
        } catch (IOException e) {
            System.err.println("Error finding files: " + e.getMessage());
        }
        return files;
    }
}
