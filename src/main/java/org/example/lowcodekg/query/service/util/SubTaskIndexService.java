package org.example.lowcodekg.query.service.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.example.lowcodekg.query.utils.Constants.SUBTASK_INDEX_NAME;
import static org.example.lowcodekg.query.utils.Constants.SUBTASK_TEMPLATE_THRESHOLD;

@Slf4j
@Service
public class SubTaskIndexService {

    @Autowired
    private ElasticSearchService esService;

    /**
     * 模板 ID → 子任务列表
     */
    private final Map<Integer, List<Document>> templateSubtasks = new HashMap<>();

    /**
     * 启动时自动构建任务模板索引
     */
    @PostConstruct
    public void init() {
        try {
            esService.setUp(SUBTASK_INDEX_NAME);
            List<Document> templates = loadTemplates("data/blog_output.json");
            for (Document template : templates) {
                template.setEmbedding(FormatUtil.ListToArray(EmbeddingUtil.embedText(template.getName())));
                esService.indexDocument(template, SUBTASK_INDEX_NAME);
            }
            log.info("Task template index initialized with {} templates.", templates.size());
        } catch (Exception e) {
            log.error("Failed to init task template index", e);
        }
    }

    /**
     * 从 JSON 加载任务模板，每个模板作为一条 ES 文档，子任务存入内存 map
     */
    private List<Document> loadTemplates(String resourcePath) {
        List<Document> templates = new ArrayList<>();

        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject root = JSON.parseObject(content);
            JSONArray predicted = root.getJSONArray("predicted");

            for (int i = 0; i < predicted.size(); i++) {
                JSONObject entry = predicted.getJSONObject(i);
                String query = entry.getString("query");
                Object taskField = entry.get("task");

                if (query == null || !(taskField instanceof JSONArray taskArray)) {
                    continue;
                }

                List<Document> subtasks = new ArrayList<>();
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

                    Document doc = new Document();
                    doc.setId(i + "_" + j);
                    doc.setName(name);
                    doc.setContent(description);
                    doc.setLabel("SubTask");
                    subtasks.add(doc);
                }

                if (!subtasks.isEmpty()) {
                    templateSubtasks.put(i, subtasks);

                    Document template = new Document();
                    template.setId(String.valueOf(i));
                    template.setName(query);
                    template.setContent(query);
                    template.setLabel("TaskTemplate");
                    templates.add(template);
                }
            }
        } catch (Exception e) {
            log.error("Error loading task templates", e);
        }
        return templates;
    }

    /**
     * 搜索最匹配的任务模板，返回其子任务列表
     * 若无模板匹配（低于阈值），返回空列表，由调用方 fallback
     */
    public List<Document> searchSubTasks(String query) {
        try {
            float[] vector = FormatUtil.ListToArray(EmbeddingUtil.embedText(query));
            List<Document> matches = esService.hybridSearch(query, vector,
                    1, SUBTASK_TEMPLATE_THRESHOLD, 0.3, SUBTASK_INDEX_NAME);

            if (matches.isEmpty()) {
                return Collections.emptyList();
            }

            Document bestTemplate = matches.get(0);
            int templateId = Integer.parseInt(bestTemplate.getId());
            List<Document> subtasks = templateSubtasks.get(templateId);

            if (subtasks != null) {
                log.info("命中模板: {}", bestTemplate.getName());
                return subtasks;
            }
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error searching task templates", e);
            return Collections.emptyList();
        }
    }
}
