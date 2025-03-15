package org.example.lowcodekg.extraction.fusion;

import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.text.MessageFormat;
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
    List<Map<String, String>> frontEndRecord = new ArrayList<>();
    List<Map<String, String>> backEndRecord = new ArrayList<>();

    @Override
    public void extraction() {
        try {
            // 前端匹配模式
            Map<String, Object> frontendPattern = new HashMap<>();
            frontendPattern.put("regex", "(axios|this\\.axios|fetch)\\.(get|post|put|delete)\\s*\\(\\s*['\"`]([^'\"`]*)");
            frontendPattern.put("extensions", Arrays.asList("*.vue", "*.js"));

            // 遍历前端目录，并使用正则表达式匹配接口请求，并关联到Neo4j实体
            for(String frontEndPath: this.getDataDir()) {
                // 清空记录
                frontEndRecord.clear();
                backEndRecord.clear();

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
                // 从Neo4j数据库查询Workflow实体
                processBackendFile();

                // 关联前端页面与后端工作流实体
                linkPageAndWorkflow();
            }
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
            String nodeId = String.valueOf(findPageEntityByFilePath(relativePath.toString()));

            // 创建正则表达式匹配器
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(content.toString());

            // 查找所有匹配项，并匹配Neo4j数据库中节点
            while (matcher.find()) {
                String lib = matcher.group(1);      // axios/this.axios/fetch
                String method = matcher.group(2);   // HTTP方法
                String url = matcher.group(3);      // URL

                Map<String, String> entry = Map.of(
                        "type", "Frontend API Call",
                        "file", relativePath.toString(),
                        "method", method.toUpperCase(),
                        "url", url,
                        "snippet", truncateSnippet(matcher.group(0), 100),
                        "id", nodeId
                );

                frontEndRecord.add(entry);
            }
        } catch (Exception e) {
            System.err.printf("Error processing frontend file %s: %s", filepath, e.getMessage());
        }
    }

    private void processBackendFile() {
        Pattern MAPPING_PATTERN = Pattern.compile("@(Get|Post|Put|Delete|Request)Mapping\\([\"'](.*?)[\"']\\)");
        try {
            String cypher = """
                    MATCH (w:Workflow)
                    RETURN w
                    """;
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(cypher);
            while (result.hasNext()) {
                Node node = result.next().get("w").asNode();
                Optional<WorkflowEntity> optional = workflowRepo.findById(node.id());
                optional.ifPresent(workflowEntity -> {
                    String mappingUrl = workflowEntity.getMappingUrl();
                    Matcher matcher = MAPPING_PATTERN.matcher(mappingUrl);
                    if (matcher.find()) {
                        String annotation = matcher.group(1);  // 获取注解类型（Get/Post/Put/Delete/Request）
                        String url = matcher.group(2);        // 获取URL路径

                        // 确定HTTP方法
                        String method = annotation.toLowerCase().equals("request") ? "GET" : annotation.toUpperCase();

                        Map<String, String> entry = Map.of(
                                "type", "BackEnd API Call",
                                "method", method,
                                "url", url,
                                "snippet", truncateSnippet(matcher.group(0), 100),
                                "id", String.valueOf(node.id())
                        );
                        backEndRecord.add(entry);
                    }
                });
            }
        } catch (Exception e) {
            System.err.printf("Error processing backend files: %s", e.getMessage());
        }
    }

    /**
     * 基于report记录，将前端页面与后端工作流实体关联起来
     */
    private void linkPageAndWorkflow() {
        try {
            // 遍历前端记录的请求，匹配后端响应接口
            for(Map<String, String> frontendEntry: frontEndRecord) {
                String frontendUrl = frontendEntry.get("url");
                Long fid = Long.parseLong(frontendEntry.get("id"));
                for(Map<String, String> backendEntry: backEndRecord) {
                    String backendUrl = backendEntry.get("url");
                    Long bid = Long.parseLong(backendEntry.get("id"));
                    // url 尾部匹配
                    if(frontendUrl.equals(backendUrl) || backendUrl.endsWith(frontendUrl) || frontendUrl.endsWith(backendUrl)) {
                        workflowRepo.createRelationBetweenPageAndWorkflow(fid, bid);
                    }
                }
            }
        } catch (Exception e) {
            System.err.printf("Error linking page and workflow: %s", e.getMessage());
        }
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

    /**
     * 根据文件路径名查找PageTemplatae实体
     */
    private Long findPageEntityByFilePath(String filePath) {
        String cypher = MessageFormat.format( "MATCH (p:PageTemplate) WHERE p.filePath = {0} RETURN p", filePath);
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(cypher);
        if (result.hasNext()) {
            Node node = result.next().get("p").asNode();
            return node.id();
        }
        return null;
    }
}
