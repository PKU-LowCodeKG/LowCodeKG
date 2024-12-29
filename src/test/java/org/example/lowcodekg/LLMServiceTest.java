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
import org.example.lowcodekg.util.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
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

        System.out.println(llmGenerateService.generateAnswer(prompt));
    }

    @Test
    public void testVueParser() {
        String path = "/Users/chang/Documents/projects/data_projects/aurora/aurora-vue/aurora-blog/src/components/Sticky.vue";
        File vueFile = new File(path);
        System.out.println(vueFile.getName());

        PageTemplate pageTemplate = new PageTemplate();
        pageTemplate.setName(vueFile.getName());
        String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

        String templateContent = getTemplateContent(fileContent);
        if(!Objects.isNull(templateContent)) {
            Document document = Jsoup.parse(templateContent);
            Element divElement = document.selectFirst("Template");
            divElement.children().forEach(element -> {
                Component component = parseTemplate(element, null);
                pageTemplate.getComponentList().add(component);
            });
        }

        // parse script
        String scriptContent = getScriptContent(fileContent);
        if(scriptContent.length() != 0) {
            Script script = parseScript(scriptContent);
            pageTemplate.setScript(script);
        }
    }
}
