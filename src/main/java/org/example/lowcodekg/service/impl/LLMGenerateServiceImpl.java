package org.example.lowcodekg.service.impl;

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

    @Autowired
    private ChatClient chatClient;

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
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> argumentMap = new HashMap<>();
        StringBuilder codeSamples = new StringBuilder();
        for (Neo4jNode neo4jNode : nodes) {
            codeSamples.append(neo4jNode.getProperties().get("content"));
            codeSamples.append("\n\n");
        }
        argumentMap.put("query", query);
        argumentMap.put("codeSamples", codeSamples.toString());
        Prompt prompt = promptTemplate.create(argumentMap);

        System.out.println(prompt.toString());

        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
        String answer = assistantMessage.getContent();
        System.out.println(answer);
        return answer;
    }
}
