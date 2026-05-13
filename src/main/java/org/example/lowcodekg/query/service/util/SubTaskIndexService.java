package org.example.lowcodekg.query.service.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.PostConstruct;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.example.lowcodekg.query.utils.Constants.SUBTASK_INDEX_NAME;

@Service
public class SubTaskIndexService {

    @Autowired
    private ElasticSearchService esService;

    /**
     * 启动时自动检查并初始化子任务索引
     */
    @PostConstruct
    public void init() {
        try {
            esService.setUp(SUBTASK_INDEX_NAME);
            List<Document> documents = loadSubTasksFromJson("data/blog_output.json");
            for (Document doc : documents) {
                doc.setEmbedding(FormatUtil.ListToArray(EmbeddingUtil.embedText(
                        doc.getName() + " " + doc.getContent())));
                esService.indexDocument(doc, SUBTASK_INDEX_NAME);
            }
            System.out.println("SubTask index initialized with " + documents.size() + " entries.");
        } catch (Exception e) {
            System.err.println("Failed to init subtask index: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从 JSON 文件中加载所有子任务描述
     * 跳过格式残缺的条目（task 不是对象数组的）
     */
    private List<Document> loadSubTasksFromJson(String resourcePath) {
        List<Document> documents = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int idCounter = 0;

        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject root = JSON.parseObject(content);
            JSONArray predicted = root.getJSONArray("predicted");

            for (int i = 0; i < predicted.size(); i++) {
                JSONObject entry = predicted.getJSONObject(i);
                Object taskField = entry.get("task");

                if (!(taskField instanceof JSONArray taskArray)) {
                    continue;
                }
                for (int j = 0; j < taskArray.size(); j++) {
                    Object item = taskArray.get(j);
                    if (!(item instanceof JSONObject taskObj)) {
                        continue;
                    }
                    String name = taskObj.getString("name");
                    String description = taskObj.getString("description");
                    if (name == null || description == null) {
                        continue;
                    }
                    // 去重：name+description 组合唯一
                    String key = name + "|||" + description;
                    if (seen.contains(key)) {
                        continue;
                    }
                    seen.add(key);

                    Document doc = new Document();
                    doc.setId(String.valueOf(idCounter++));
                    doc.setName(name);
                    doc.setContent(description);
                    doc.setLabel("SubTask");
                    documents.add(doc);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading subtask data: " + e.getMessage());
            e.printStackTrace();
        }
        return documents;
    }

    /**
     * 根据用户需求检索匹配的子任务描述
     */
    public List<Document> searchSubTasks(String query) {
        try {
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(query));
            return esService.hybridSearch(query, vector,
                    org.example.lowcodekg.query.utils.Constants.MAX_SUBTASK_NUM,
                    0.0, 0.5, SUBTASK_INDEX_NAME);
        } catch (Exception e) {
            System.err.println("Error searching subtasks: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
