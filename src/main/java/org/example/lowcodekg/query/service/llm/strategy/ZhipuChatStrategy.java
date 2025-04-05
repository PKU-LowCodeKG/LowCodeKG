package org.example.lowcodekg.query.service.llm.strategy;

import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletion;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletionResponse;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatMessage;
import io.github.lnyocly.ai4j.service.IChatService;
import io.github.lnyocly.ai4j.service.PlatformType;
import io.github.lnyocly.ai4j.service.factor.AiService;


public class ZhipuChatStrategy implements ChatStrategy {
    private final AiService aiService;

    public ZhipuChatStrategy(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public String chat(String prompt) {
        try {
            IChatService chatService = aiService.getChatService(PlatformType.ZHIPU);
            ChatCompletion chatCompletion = ChatCompletion.builder()
//                    .model("glm-4-flash")
                    .model("glm-4-plus")
                    .message(ChatMessage.withUser(prompt))
                    .build();
            ChatCompletionResponse response = chatService.chatCompletion(chatCompletion);
            return response.getChoices().get(0).getMessage().getContent().getText();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in Zhipu chat: " + e.getMessage());
            return null;
        }
    }
}
