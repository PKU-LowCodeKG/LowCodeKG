package org.example.lowcodekg.query.service.classification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本分类数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextClassificationData {

    private String query;
    private List<String> labels;

    @Override
    public String toString() {
        return "TextClassificationData{" +
                "query='" + query + '\'' +
                ", labels=" + labels +
                '}';
    }
}