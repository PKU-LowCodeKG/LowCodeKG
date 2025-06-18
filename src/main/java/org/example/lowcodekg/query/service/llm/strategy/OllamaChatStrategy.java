package org.example.lowcodekg.query.service.llm.strategy;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class OllamaChatStrategy implements ChatStrategy {
    private final OllamaChatModel ollamaChatModel;

    public OllamaChatStrategy(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public String chat(String prompt) {
        int maxRetries = 1;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                UserMessage userMessage = UserMessage.from(prompt);
                AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
                return aiMessage.text();
            } catch (Exception e) {
                retryCount++;
                System.out.println("Ollama chat failed, retrying...");
            }
        }
        return null;
    }
}