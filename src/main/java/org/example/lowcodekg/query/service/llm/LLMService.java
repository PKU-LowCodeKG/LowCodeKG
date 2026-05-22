package org.example.lowcodekg.query.service.llm;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class LLMService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.base.url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    @Value("${deepseek.temperature:0.1}")
    private double temperature;

    @Value("${deepseek.max.tokens:256}")
    private int maxTokens;

    private OpenAiChatModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(30))
                .build();
        log.info("LLM service initialized: {} @ {}", model, baseUrl);
    }

    public String chat(String systemPrompt, String userMessage) {
        try {
            dev.langchain4j.data.message.SystemMessage sysMsg =
                    dev.langchain4j.data.message.SystemMessage.from(systemPrompt);
            dev.langchain4j.data.message.UserMessage usrMsg =
                    dev.langchain4j.data.message.UserMessage.from(userMessage);

            var response = chatModel.generate(sysMsg, usrMsg);
            return response.content().text();
        } catch (Exception e) {
            log.error("LLM call failed", e);
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }
}
