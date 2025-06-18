package org.example.lowcodekg.extraction;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.extraction.page.PageExtractor;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.model.schema.entity.page.Component;
import org.example.lowcodekg.model.schema.entity.page.ConfigItem;
import org.example.lowcodekg.model.schema.entity.page.PageTemplate;
import org.example.lowcodekg.query.service.util.summarize.FuncGenerate;
import org.example.lowcodekg.service.FunctionalityGenService;
import org.example.lowcodekg.service.ClineService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.example.lowcodekg.common.util.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.lowcodekg.common.util.PageParserUtil.getTemplateContent;

@SpringBootTest
public class ExtractorTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private ClineService clineService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private FunctionalityGenService functionalityGenService;
    @Autowired
    private ComponentRepo componentRepo;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private FuncGenerate funcGenerate;

    @Test
    public void testKG() {
        // 读取JSON文件中的统计数据
        String jsonPath = "src/main/resources/test/kg_statistic.json";
        String jsonContent = FileUtil.readFile(jsonPath);
        JSONObject fileStats = JSONObject.parseObject(jsonContent);
        
        // 从Neo4j读取统计数据
        JSONObject neo4jStats = new JSONObject();
        JSONObject entityStats = new JSONObject();
        JSONObject relationStats = new JSONObject();
        
        // 统计实体
        String nodeCountCypher = "MATCH (n) RETURN DISTINCT labels(n) as label, count(*) as count";
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result nodeResult = runner.run(nodeCountCypher);
        while(nodeResult.hasNext()) {
            var record = nodeResult.next();
            String label = String.join("+", record.get("label").asList().stream().map(Object::toString).toList());
            entityStats.put(label, record.get("count").asInt());
        }
        
        // 统计关系
        String relCountCypher = "MATCH ()-[r]->() RETURN DISTINCT type(r) as type, count(*) as count";
        Result relResult = runner.run(relCountCypher);
        while(relResult.hasNext()) {
            var record = relResult.next();
            relationStats.put(record.get("type").asString(), record.get("count").asInt());
        }
        
        neo4jStats.put("entities", entityStats);
        neo4jStats.put("relations", relationStats);
        
        // 比较两个JSON对象是否完全相同
        boolean isEqual = fileStats.equals(neo4jStats);
        
        if (isEqual) {
            System.out.println("测试通过：JSON文件数据与Neo4j数据完全一致");
        } else {
            System.out.println("测试不通过：数据不一致");
            System.out.println("\nJSON文件中的数据：");
            System.out.println(fileStats.toJSONString());
            System.out.println("\nNeo4j中的数据：");
            System.out.println(neo4jStats.toJSONString());
        }
        
        assert isEqual : "统计数据不一致";
    }

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
        String codeContent = """
                @PostMapping("/account")
                	public Result account(@RequestBody User user, @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {
                		boolean res = userService.changeAccount(user, jwt);
                		return res ? Result.ok("修改成功") : Result.error("修改失败");
                	}
                
                @Override
                	public boolean changeAccount(User user, String jwt) {
                		String username = JwtUtils.getTokenBody(jwt).getSubject();
                		user.setPassword(HashUtils.getBC(user.getPassword()));
                		if (userMapper.updateUserByUsername(username, user) != 1) {
                			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                			return false;
                		}
                		return true;
                	}
                
                <!--按username修改-->
                    <update id="updateUserByUsername">
                        update user set username=#{user.username}, password=#{user.password}, update_time=now() where username=#{username}
                    </update>
                
                public static Claims getTokenBody(String token) {
                		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token.replace("Bearer", "")).getBody();
                		return claims;
                	}
                
                public static String getBC(CharSequence rawPassword) {
                		return bCryptPasswordEncoder.encode(rawPassword);
                	}
                
                """;
        String prompt = """
                    You are an expert in programming with a thorough understanding of software projects.
                    The content below provides the method calls and data objects involved in implementing a certain function request within a software project.
                    Based on the code provided, please summarize the implemented function and the technological frameworks, third-party libraries, etc., used during the implementation.
                    Your results should address three aspects: "功能概括," "执行逻辑," and "技术特征." Specifically:
                    * **功能概括**: Provide a concise description of the implemented function without involving technical details, keep it as short as possible.
                    * **执行逻辑**: Describe the overall process of code execution, minimizing technical details.
                    * **技术特征**: Mention any technological frameworks, third-party libraries, tools, etc., involved during the code execution.
                    
                    The code content you need to explain is as follows:
                    {codeContent}
                    
                    Please ensure the output is concise and not too lengthy, also in Chinese, while strictly following the JSON format below without including any additional content:
                    ```json
                    {
                        "功能概括": "",
                        "执行逻辑": "",
                        "技术特征": ""
                    }
                    ```
                    """;
        prompt = prompt.replace("{codeContent}", codeContent);
        String result = llmGenerateService.generateAnswer(prompt);
        System.out.println(result);
        Pattern p = Pattern.compile("```json\\s*(\\{[.\\d\\w\\s\\n\\D]*\\})\\s*```");
        Matcher m = p.matcher(result);
        if (m.find()) {
            result = m.group(1);
            System.out.println(result);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        System.out.println(jsonObject.getString("功能概括"));
    }

    @Test
    public void testFunctionGenerator() {
        String cypher = """
                    MATCH (n:Workflow)
                    RETURN n
                    """;
        QueryRunner runner = neo4jClient.getQueryRunner();
        Result result = runner.run(cypher);
        while(result.hasNext()) {
            Node node = result.next().get("n").asNode();
            Optional<WorkflowEntity> optional = workflowRepo.findById(node.id());
            optional.ifPresent(workflowEntity -> {
                    funcGenerate.genWorkflowFunc(workflowEntity);
            });
        }
    }
}
