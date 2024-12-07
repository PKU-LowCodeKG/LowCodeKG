package org.example.lowcodekg;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LLMServiceTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    public void test() {
        String prompt = "What is the capital of France?";
        UserMessage userMessage = UserMessage.from(prompt);
        AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
        String answer = aiMessage.text();
        System.out.println(answer);
    }
}
