package org.example.lowcodekg.service;

import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PromptTest {
    @Autowired
    private TaskMatching taskMatch;

    @Test
    void testSanity() {
        // 仅验证 Spring 上下文能正常加载
        System.out.println("Context loaded successfully.");
    }
}
