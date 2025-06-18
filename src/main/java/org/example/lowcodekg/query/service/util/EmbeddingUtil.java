package org.example.lowcodekg.query.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
     * 调用本地ollama部署的模型进行文本嵌入表示
     * @param text
     * @return
     * @throws Exception
     */
    public static float[] getEmbedding(String text) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://localhost:5000/embed");

            // 设置JSON请求体
            String json = "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";
            post.setEntity(new StringEntity(json));
            post.setHeader("Content-Type", "application/json");

            // 获取响应并解析
            String response = client.execute(post, httpResponse ->
                    new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))
                            .lines().collect(Collectors.joining("\n")));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            JsonNode embeddingNode = rootNode.get("embedding");
            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = embeddingNode.get(i).floatValue();
            }
            return embedding;
        }
    }

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
    public static double cosineSimilarity(List<Float> vector1, List<Float> vector2) {
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

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0.0 || normB == 0.0) {
            return 0.0; // 如果任意向量的范数为0，相似度为0
        }

        return dotProduct / (normA * normB);
    }

    public static void main(String[] args) {
        String text1 = "在null的条件下通过执行点击按钮的操作，处理订单数据，得到订单已提交的结果";
        String text2 = "在null的条件下通过执行null的操作，处理订单，得到订单提交的结果";
        String text3 = "在用户登录的条件下通过执行填写邮箱的操作，处理用户信息，得到用户通过邮箱注册的结果";
        try {
//            float[] embedding1 = getEmbedding(text1);
//            float[] embedding2 = getEmbedding(text2);
//            float[] embedding3 = getEmbedding(text3);
//            System.out.println(cosineSimilarity(embedding1, embedding2));
//            System.out.println(cosineSimilarity(embedding1, embedding3));
//            System.out.println(cosineSimilarity(embedding2, embedding3));

            System.out.println(calculateSimilarity(text1, text2));
            System.out.println(calculateSimilarity(text1, text3));
            System.out.println(calculateSimilarity(text2, text3));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
