package org.example.lowcodekg.query.service.util;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
//import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public class LLMService {
//    public String generateAnswer(String prompt) {
//        try {
//            ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
//                    .apiKey("AIzaSyAHvsze-iYKHhX299_DjUOCp51QQ_Z6h2s")
//                    .modelName("gemini-pro")  // Changed model name to the correct one
//                    .temperature(0.7)  // Added temperature for better control
//                    .build();
//
//            ChatResponse chatResponse = gemini.chat(ChatRequest.builder()
//                    .messages(UserMessage.from(prompt))
//                    .build());
//
//            return chatResponse.aiMessage().text();
//        } catch (Exception e) {
//            // More specific error handling
//            System.err.println("Error while generating response: " + e.getMessage());
//            throw new RuntimeException("Failed to generate response from Gemini API", e);
//        }
//    }
}
