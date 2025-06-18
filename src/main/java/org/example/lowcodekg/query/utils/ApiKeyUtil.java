package org.example.lowcodekg.query.utils;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

@Component
public class ApiKeyUtil {
    private static final String API_KEY_PATH = "D:\\master\\Data\\api_key.json";
    private static JsonNode apiKeyConfig;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            apiKeyConfig = mapper.readTree(new File(API_KEY_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read API key configuration file", e);
        }
    }

    public static String getZhipuApiKey() {
        return apiKeyConfig.get("zhipu").get("key").asText();
    }

    public static String getZhipuModel() {
        return apiKeyConfig.get("zhipu").get("model").asText();
    }

    public static String getOpenAiApiKey() {
        return apiKeyConfig.get("openai").get("key").asText();
    }

    public static String getOpenAiModel() {
        return apiKeyConfig.get("openai").get("model").asText();
    }

    public static String getDeepSeekApiKey() {
        return apiKeyConfig.get("deepseek").get("key").asText();
    }

    public static String getDeepSeekModel() {
        return apiKeyConfig.get("deepseek").get("model").asText();
    }
}
