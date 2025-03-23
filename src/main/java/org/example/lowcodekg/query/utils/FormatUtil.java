package org.example.lowcodekg.query.utils;

import org.example.lowcodekg.model.dao.Describable;
import org.example.lowcodekg.model.dao.es.document.Document;

import java.util.List;

/**
 * @Description 数据对象类型转换
 * @Author Sherloque
 * @Date 2025/3/23 14:47
 */
public class FormatUtil {

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
