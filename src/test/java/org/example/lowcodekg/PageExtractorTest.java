package org.example.lowcodekg;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.dao.neo4j.entity.page.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.extraction.page.PageExtractor;
import org.example.lowcodekg.schema.entity.page.Component;
import org.example.lowcodekg.schema.entity.page.ConfigItem;
import org.example.lowcodekg.schema.entity.page.PageTemplate;
import org.example.lowcodekg.service.FunctionalityGenService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.example.lowcodekg.util.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import static org.example.lowcodekg.util.PageParserUtil.getTemplateContent;

@SpringBootTest
public class PageExtractorTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private FunctionalityGenService functionalityGenService;
    @Autowired
    private ComponentRepo componentRepo;

    @Test
    public void test() {
        String prompt = """
                给定下面的代码内容，你的任务是对其进行解析返回一个json对象。注意，如果key对应的value包含了表达式或函数调用，将其转为字符串格式
                比如：对于
                "headers": {
                    "Authorization": "Bearer " + sessionStorage.getItem('token')
               }，应该表示为：
                "headers": {
                    "Authorization": "'Bearer ' + sessionStorage.getItem('token')"
                  }
                
                下面是给出的代码片段，请返回json结果:
                {content}
                
                """;
        String content = """
                 {       radioValue: 1,      cycle01: 1,      cycle02: 2,      average01: 0,      average02: 1,      checkboxList: [],      checkNum: this.$options.propsData.check    }
                
                """;
        prompt = prompt.replace("{content}", content);
        JSONObject jsonObject = new JSONObject();
        try {
            prompt = prompt.replace("{content}", content);
            String answer = llmGenerateService.generateAnswer(prompt);
            if(answer.contains("```json")) {
                answer = answer.substring(answer.indexOf("```json") + 7, answer.lastIndexOf("```"));
            }
            System.out.println(answer);
            jsonObject = JSONObject.parseObject(answer);
            jsonObject.forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVueParser() {
        String path = "";
        path = "/Users/chang/Documents/projects/data_projects/NBlog/blog-cms/src/views/page/FriendList.vue";
        File vueFile = new File(path);
        System.out.println(vueFile.getName());

        PageTemplate pageTemplate = new PageTemplate();
        pageTemplate.setName(vueFile.getName());
        String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

        PageExtractor pageExtractor = new PageExtractor();

        // parse template
        String templateContent = getTemplateContent(fileContent);
        if(!Objects.isNull(templateContent)) {
            Document document = Jsoup.parse(templateContent);
            Element divElement = document.selectFirst("Template");
            divElement.children().forEach(element -> {
                Component component = pageExtractor.parseTemplate(element, null);
                pageTemplate.getComponentList().add(component);
            });
        }
        for(Component component: pageTemplate.getComponentList()) {
            for(ConfigItem configItem: component.getConfigItemList()) {
                System.out.println("config item: " + configItem.getCode() + " " + configItem.getValue());
            }
        }

        // parse script
//        String content = pageExtractor.getScriptContent(fileContent);
//        if(content.length() != 0) {
//            Script script = new Script();
//            script.setContent(content);
//
//            // parse import components
//            JSONObject importsList = pageExtractor.parseImportsComponent(content);
//            script.setImportsComponentList(importsList.toString());
//
//            // parse data
////            JSONObject data = pageExtractor.parseScriptData(content);
////            script.setDataList(data);
//
//            // parse methods
//            List<Script.ScriptMethod> methodList = pageExtractor.parseScriptMethod(content);
//            script.setMethodList(methodList);
//            pageTemplate.setScript(script);
//        }
    }

    @Test
    public void textFunctionality() {
        String cypher = MessageFormat.format("""
                MATCH (n:PageTemplate)-[:CONTAIN]->(m:Component)
                    WHERE id(n) = {0}
                    RETURN m
                """, String.format("%d", 1343));
        Result res = neo4jClient.getQueryRunner().run(cypher);
        while(res.hasNext()) {
            Node node = res.next().get("m").asNode();
            Optional<ComponentEntity> entity = componentRepo.findById(node.id());
            entity.ifPresent(componentEntity -> {
//                functionalityGenService.generatePageFunctionality(pageEntity);
                System.out.println(componentEntity.getName());
            });
        }
    }
}
