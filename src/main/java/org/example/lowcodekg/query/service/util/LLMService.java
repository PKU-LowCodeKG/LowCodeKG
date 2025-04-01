package org.example.lowcodekg.query.service.util;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LLMService {

    @Value("${gemini.api.key}")
    private String apiKey;
    
    public String chat(String prompt) {
        try {
            ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gemini-2.0-flash")
                    .build();

            String response = gemini.generate(prompt);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I'm having trouble understanding your request. Please try again.";
        }
    }
}
