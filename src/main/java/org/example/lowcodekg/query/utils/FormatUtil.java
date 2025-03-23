package org.example.lowcodekg.query.utils;

import org.example.lowcodekg.model.dao.Describable;
import org.example.lowcodekg.model.dao.es.document.Document;

/**
 * @Description 数据对象类型转换
 * @Author Sherloque
 * @Date 2025/3/23 14:47
 */
public class FormatUtil {

    public static Document entityToDocument(Describable entity) {
        Document document = new Document();
        document.setId(entity.getId().toString());
        document.setName(entity.getName());
        document.setContent(entity.getDescription());
        // TODO: 调用embedding model

        return document;
    }
}
