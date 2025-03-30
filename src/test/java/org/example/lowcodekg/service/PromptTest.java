package org.example.lowcodekg.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PromptTest {
    @Autowired
    private LLMGenerateService llmService;

    @Test
    void test() {
        String prompt = """
                
                """;

        String answer = llmService.generateAnswer(prompt);

        System.out.println(answer);
    }
}
