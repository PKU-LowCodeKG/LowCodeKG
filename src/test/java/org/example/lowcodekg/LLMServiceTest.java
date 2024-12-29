package org.example.lowcodekg;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.schema.entity.page.Component;
import org.example.lowcodekg.schema.entity.page.PageTemplate;
import org.example.lowcodekg.schema.entity.page.Script;
import org.example.lowcodekg.service.LLMGenerateService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.example.lowcodekg.extraction.page.PageExtractor.*;

@SpringBootTest
public class LLMServiceTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;
    @Autowired
    private LLMGenerateService llmGenerateService;

    @Test
    public void test() {
        String prompt = "What is the capital of France?";
        UserMessage userMessage = UserMessage.from(prompt);
        AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
        String answer = aiMessage.text();
        System.out.println(answer);
    }
}
