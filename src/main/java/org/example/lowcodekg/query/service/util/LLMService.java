package org.example.lowcodekg.query.service.util;

import io.github.lnyocly.ai4j.config.DeepSeekConfig;
import io.github.lnyocly.ai4j.config.ZhipuConfig;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletion;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletionResponse;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatMessage;
import io.github.lnyocly.ai4j.service.Configuration;
import io.github.lnyocly.ai4j.service.IChatService;
import io.github.lnyocly.ai4j.service.PlatformType;
import io.github.lnyocly.ai4j.service.factor.AiService;
import org.example.lowcodekg.query.utils.ApiKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

@Service
public class LLMService {

    @Autowired
    private AiService aiService;

    public ChatCompletionResponse chat(String prompt) {
        try {
//            Configuration config = new Configuration();
            // Configure OkHttpClient with longer timeouts
//            OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(100, TimeUnit.SECONDS)
//                .writeTimeout(100, TimeUnit.SECONDS)
//                .readTimeout(100, TimeUnit.SECONDS)
//                .build();
//            config.setOkHttpClient(client);

            // zhipu
//            String apiKey = ApiKeyUtil.getZhipuApiKey();
//            String model = ApiKeyUtil.getZhipuModel();
//            ZhipuConfig zhipuConfig = new ZhipuConfig();
//            zhipuConfig.setApiKey(apiKey);
//            config.setZhipuConfig(zhipuConfig);

            // deepseek
//            String deepseekApiKey = ApiKeyUtil.getDeepSeekApiKey();
//            String model = ApiKeyUtil.getDeepSeekModel();
//            DeepSeekConfig deepseekConfig = new DeepSeekConfig();
//            deepseekConfig.setApiKey(deepseekApiKey);
//            config.setDeepSeekConfig(deepseekConfig);
            
            IChatService chatService =
//                    new AiService(config)
                    aiService
                    .getChatService(PlatformType.ZHIPU);
            ChatCompletion chatCompletion = ChatCompletion.builder()
//                    .model(model)
                    .model("glm-4-flash")
                    .message(ChatMessage.withUser(prompt))
                    .build();
            ChatCompletionResponse response = chatService.chatCompletion(chatCompletion);
            System.out.println("Response: " + response);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in chat: " + e.getMessage());
            return null;
        }
    }
}
