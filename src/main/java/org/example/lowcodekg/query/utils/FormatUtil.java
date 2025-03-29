package org.example.lowcodekg.query.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.dao.Describable;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dto.Neo4jNode;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.example.lowcodekg.query.utils.Constants.resultSavePath;

/**
 * @Description 数据对象类型转换
 * @Author Sherloque
 * @Date 2025/3/23 14:47
 */
public class FormatUtil {

    /**
     * 将查询处理过程的中间及结果数据保存到本地
     * @param query
     * @param graph
     * @param nodeList
     */
    public static void saveTask(String query, TaskGraph graph, List<Node> nodeList) {
        try {
            JSONObject result = new JSONObject();
            result.put("query", query);

            JSONArray graphJson = new JSONArray();
            for(Task task : graph.getTasks().values()) {
                JSONObject taskJson = new JSONObject();
                taskJson.put("id", task.getId());
                taskJson.put("name", task.getName());
                taskJson.put("description", task.getDescription());
                taskJson.put("irList", task.getIrList());
                graphJson.add(taskJson);
            }
            result.put("tasks", graphJson);

            JSONArray nodeJsonArray = new JSONArray();
            for(Node node : nodeList) {
                JSONObject nodeJson = new JSONObject();
                nodeJson.put("id", node.getId());
                nodeJson.put("name", node.getName());
                nodeJson.put("label", node.getLabel());
                nodeJson.put("content", node.getContent());
                nodeJson.put("description", node.getDescription());
                nodeJson.put("irList", node.getIrList());
                nodeJsonArray.add(nodeJson);
            }
            result.put("resources", nodeJsonArray);

            // 保存到本地，以追加的形式写入文件
            try (FileWriter fileWriter = new FileWriter(resultSavePath, true)) {
                if (fileWriter.getEncoding() != null) {
                    fileWriter.write(result.toJSONString());
                    fileWriter.write("\n"); // 换行以便多个记录之间有间隔
                }
            }

        } catch (Exception e) {
            System.err.println("Error in saveTask: " + e.getMessage());
            throw new RuntimeException("Error in saveTask: " + e.getMessage());
        }
    }

    /**
     * 将大模型返回结果中的json片段提取出来
     */
    public static String extractJson(String text) {
        if(text.contains("```json")) {
            text = text.substring(text.indexOf("```json") + 7, text.lastIndexOf("```"));
        } else {
            throw new RuntimeException("Json format error:\n" + text);
        }
        return text;
    }

    /**
     * 将实体对象转换为文档对象,以用于创建ES索引
     */
    public static Document entityToDocument(Describable entity) {
        Document document = new Document();
        document.setId(entity.getId().toString());
        document.setName(entity.getName());
        document.setContent(entity.getDescription());
        document.setEmbedding(FormatUtil.ListToArray(entity.getEmbedding()));
        return document;
    }

    public static float[] ListToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
