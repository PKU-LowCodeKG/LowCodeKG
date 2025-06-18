package org.example.lowcodekg.query.service.llm;

import dev.langchain4j.model.ollama.OllamaChatModel;
import io.github.lnyocly.ai4j.service.factor.AiService;
import org.example.lowcodekg.common.config.LLMConfig;
import org.example.lowcodekg.query.service.llm.strategy.ChatStrategy;
import org.example.lowcodekg.query.service.llm.strategy.DeepseekChatStrategy;
import org.example.lowcodekg.query.service.llm.strategy.OllamaChatStrategy;
import org.example.lowcodekg.query.service.llm.strategy.ZhipuChatStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LLMService {

    private final Map<String, ChatStrategy> strategyMap = new HashMap<>();
    private final LLMConfig llmConfig;

    @Autowired
    public LLMService(AiService aiService, OllamaChatModel ollamaChatModel, LLMConfig llmConfig) {
        strategyMap.put("zhipu", new ZhipuChatStrategy(aiService));
        strategyMap.put("deepseek", new DeepseekChatStrategy(aiService));
        strategyMap.put("ollama", new OllamaChatStrategy(ollamaChatModel));
        this.llmConfig = llmConfig;
    }

    public String chat(String prompt) {
        return chat(llmConfig.getModelType(), prompt);
    }

    public String chat(String model, String prompt) {
        ChatStrategy strategy = strategyMap.get(model.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported model: " + model);
        }
        return strategy.chat(prompt);
    }
}
