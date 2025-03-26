package org.example.lowcodekg.query.utils;

import org.example.lowcodekg.model.dao.Describable;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dto.Neo4jNode;
import org.example.lowcodekg.query.model.Node;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @Description 数据对象类型转换
 * @Author Sherloque
 * @Date 2025/3/23 14:47
 */
public class FormatUtil {

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
