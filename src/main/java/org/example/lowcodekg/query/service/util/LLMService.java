package org.example.lowcodekg.query.service.util;

import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletion;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletionResponse;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatMessage;
import io.github.lnyocly.ai4j.service.IChatService;
import io.github.lnyocly.ai4j.service.PlatformType;
import io.github.lnyocly.ai4j.service.factor.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LLMService {

    @Autowired
    private AiService aiService;

    public void chat(String prompt) {
        try {
            IChatService chatService = aiService.getChatService(PlatformType.ZHIPU);
            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .model("glm-4-flash")
                    .message(ChatMessage.withUser(prompt))
                    .build();
            ChatCompletionResponse response = chatService.chatCompletion(chatCompletion);
            System.out.println(response);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in chat: " + e.getMessage());
        }
    }
}
