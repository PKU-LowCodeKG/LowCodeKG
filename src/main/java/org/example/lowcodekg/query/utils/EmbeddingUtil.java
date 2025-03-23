package org.example.lowcodekg.query.utils;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 文本嵌入表示
 * @Author Sherloque
 * @Date 2025/3/23 16:19
 */
public class EmbeddingUtil {

    private static final EmbeddingModel embeddingModel = new BgeSmallZhV15QuantizedEmbeddingModel();


    /**
     * 对单个文本进行嵌入
     */
    public static List<Float> embedText(String text) {
        Embedding embedding = embeddingModel.embed(text).content();
        return embedding.vectorAsList();
    }

    /**
     * 批量对文本进行嵌入
     */
    public static List<List<Float>> embedTexts(List<String> texts) {
        List<TextSegment> textSegments = texts.stream()
                .map(TextSegment::from)
                .collect(Collectors.toList());
        List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content(); // 直接获取 List<Embedding>

        return embeddings.stream()
                .map(Embedding::vectorAsList)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个文本之间的相似度
     */
    public static double calculateSimilarity(String text1, String text2) {
        Embedding embedding1 = embeddingModel.embed(text1).content();
        Embedding embedding2 = embeddingModel.embed(text2).content();

        return cosineSimilarity(embedding1.vectorAsList(), embedding2.vectorAsList());
    }

    /**
     * 手动计算余弦相似度
     */
    private static double cosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
