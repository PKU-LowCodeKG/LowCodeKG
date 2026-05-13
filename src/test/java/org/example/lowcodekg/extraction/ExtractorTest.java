package org.example.lowcodekg.extraction;

import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.extraction.page.PageExtractor;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.model.schema.entity.page.Component;
import org.example.lowcodekg.model.schema.entity.page.ConfigItem;
import org.example.lowcodekg.model.schema.entity.page.PageTemplate;
import org.example.lowcodekg.query.service.util.summarize.FuncGenerate;
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

import static org.example.lowcodekg.common.util.PageParserUtil.getTemplateContent;

@SpringBootTest
public class ExtractorTest {

    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private PageRepo pageRepo;
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
