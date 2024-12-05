package org.example.lowcodekg.service.impl;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LLMGenerateServiceImpl implements LLMGenerateService {

//    @Autowired
//    private ChatClient chatClient;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Override
    public String graphPromptToCode(String query, List<Neo4jNode> nodes) {
        String template = """
                你是一名程序员，现在正在开发一个个人博客系统的后端。
                当前，你要实现的功能为【{query}】。
                在编写代码之前，你在网络上搜索到了一些可能与该功能相关的代码片段，
                请参考借鉴这些代码片段，最终完整地实现【{query}】功能。
                请注意：你的答案只能包含代码，代码应该完整，请不要包含任何解释说明的文字。
                
                可参考的代码:
                {codeSamples}
                
                你编写的代码：
                """;

        Map<String, Object> argumentMap = new HashMap<>();
        StringBuilder codeSamples = new StringBuilder();
        for (Neo4jNode neo4jNode : nodes) {
            codeSamples.append(neo4jNode.getProperties().get("content"));
            codeSamples.append("\n\n");
        }
        argumentMap.put("query", query);
        argumentMap.put("codeSamples", codeSamples.toString());
        String prompt = StrUtil.format(template, argumentMap);

        System.out.println(prompt);

        UserMessage userMessage = UserMessage.from(prompt);
        AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
        String answer = aiMessage.text();
        System.out.println(answer);
        return answer;
    }
}
