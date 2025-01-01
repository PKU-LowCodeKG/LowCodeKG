package org.example.lowcodekg.extraction.page;

import cn.hutool.db.Page;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.dao.neo4j.entity.page.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptEntity;
import org.example.lowcodekg.dao.neo4j.repository.ScriptMethodRepo;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.schema.entity.page.*;
import org.example.lowcodekg.service.LLMGenerateService;
import org.example.lowcodekg.util.FileUtil;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.ScriptData;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * 前端页面抽取
 * 目前只实现对Vue框架的解析
 */
@Service
public class PageExtractor extends KnowledgeExtractor {

    private Map<String, PageEntity> pageEntityMap = new HashMap<>();
    private Map<String, PageTemplate> pageTemplateMap = new HashMap<>();

    @Override
    public void extraction() {
        for(String filePath: this.getDataDir()) {
            Collection<File> vueFiles = FileUtils.listFiles(new File(filePath), new String[]{"vue"}, true);
            for(File vueFile: vueFiles) {
                System.out.println("---parse file: " + vueFile.getAbsolutePath());

                // each .vue file parsed as one PageTemplate entity
                PageTemplate pageTemplate = new PageTemplate();
                String name = vueFile.getName().substring(0, vueFile.getName().length()-4);
                String fullName = vueFile.getAbsolutePath().replace(filePath, "");
                pageTemplate.setName(name);
                pageTemplate.setFullName(fullName);
                String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

                // parse template
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
                    script.setName(name);
                    pageTemplate.setScript(script);
                }
                // neo4j store
                storeNeo4j(pageTemplate);
            }
            // create relationships among page entities
            pageEntityMap.values().forEach(pageEntity -> {
                PageTemplate pageTemplate = pageTemplateMap.get(pageEntity.getName());
                pageTemplate.findDependedPage();
                pageTemplate.getDependedPageList().forEach(dependedPageName -> {
                   if(pageEntityMap.containsKey(dependedPageName)) {
                       pageRepo.createRelationOfDependedPage(pageEntity.getId(), pageEntityMap.get(dependedPageName).getId());
                   }
                });
            });
        }
    }

    /**
     * store page-related entities and relationships in neo4j
     */
    private PageEntity storeNeo4j(PageTemplate pageTemplate) {
        try {
            PageEntity pageEntity = pageTemplate.createPageEntity(pageRepo);
            pageEntityMap.put(pageEntity.getName(), pageEntity);
            pageTemplateMap.put(pageEntity.getName(), pageTemplate);
            // component entity
            for(Component component: pageTemplate.getComponentList()) {
                ComponentEntity componentEntity = component.createComponentEntity(componentRepo);
                pageEntity.getComponentList().add(componentEntity);
                pageRepo.createRelationOfContainedComponent(pageEntity.getId(), componentEntity.getId());
            }
            // script entity
            if(!Objects.isNull(pageTemplate.getScript())) {
                ScriptEntity scriptEntity = pageTemplate.getScript().createScriptEntity(scriptRepo, scriptMethodRepo);
                pageRepo.createRelationOfContainedScript(pageEntity.getId(), scriptEntity.getId());
            }
            return pageEntity;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in PageExtractor storeNeo4j: " + e.getMessage());
            return null;
        }
    }

    public String getTemplateContent(String fileContent) {
        Pattern pattern = Pattern.compile("<template>(.*?)</template>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(0).trim(); // 返回去除前后空白的模板内容
        } else {
            return null;
        }
    }

    public String getScriptContent(String fileContent) {
        StringBuilder scriptContent = new StringBuilder();
        List<String> lines = Arrays.asList(fileContent.split("\n"));
        for(int i = 0;i < lines.size();i++) {
            if(lines.get(i).contains("<script")) {
                int j = i + 1;
                while(j < lines.size() && !lines.get(j).contains("</script>")) {
                    scriptContent.append(lines.get(j)).append("\n");
                    j++;
                }
                break;
            }
        }
        return scriptContent.toString();
    }

    public Component parseTemplate(Element element, Element parent) {
        Component component = new Component();
        component.setName(element.tagName());
        component.setText(element.text());
        component.setContent(element.toString());

        element.attributes().forEach(attr -> {
            ConfigItem config = new ConfigItem(attr.getKey(), attr.getValue());
            component.getConfigItemList().add(config);
        });
        for (Element child : element.children()) {
            Component childComponent = parseTemplate(child, element);
            component.getChildren().add(childComponent);
        }
        return component;
    }

    public Script parseScript(String content) {
        Script script = new Script();
        script.setContent(content);

        // parse import components
        JSONObject importsList = parseImportsComponent(content);
        script.setImportsComponentList(importsList.toString());


        // parse data
        JSONObject data = parseScriptData(content);
        script.setDataList(data);

        // parse methods
        List<Script.ScriptMethod> methodList = parseScriptMethod(content);
        script.setMethodList(methodList);

        return script;
    }

    public JSONObject parseImportsComponent(String content) {
        try {
            JSONObject importsList = new JSONObject();
            String importPattern = "import\\s*\\{?\\s*([\\w,\\s]+)\\s*\\}?\\s*from\\s*['\"]([^'\"]+)['\"]";
            Pattern pattern = Pattern.compile(importPattern);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String names = matcher.group(1).trim();
                String path = matcher.group(2).trim();

                String[] nameArray = names.split("\\s*,\\s*");
                for (String name : nameArray) {
                    importsList.put(name, path);
                }
            }
            return importsList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject parseScriptData(String content) {
        // get data block
        String dataBlock = getScriptData(content);
        if(Objects.isNull(dataBlock)) {
            return null;
        }
        // json format
        String prompt = """
                给定下面的代码内容，你的任务是对其进行解析返回一个json对象。注意，如果key对应的value包含了表达式或函数调用，将其转为字符串格式
                比如：对于
                "headers": {
                    "Authorization": "Bearer " + sessionStorage.getItem('token')
               }，应该表示为：
                "headers": {
                    "Authorization": "'Bearer ' + sessionStorage.getItem('token')"
                  }
                
                下面是给出的代码片段:
                {content}
                """;
        JSONObject jsonObject = new JSONObject();
        try {
            prompt = prompt.replace("{content}", dataBlock);
            String answer = llmGenerateService.generateAnswer(prompt);
            if(answer.contains("```json")) {
                answer = answer.substring(answer.indexOf("```json") + 7, answer.lastIndexOf("```"));
            }
            jsonObject = JSONObject.parseObject(answer);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("script data json format error:\n" + dataBlock);
        }
        return jsonObject;
    }

    public String getScriptData(String content) {
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("data")) {
                if((lineList.get(i).contains("data()") || lineList.get(i).contains("function"))
                        && i+1 < lineList.size() && lineList.get(i+1).contains("return {")) {
                    int j = i + 2;
                    while (j < lineList.size()) {
                        dataBlock.append(lineList.get(j));
                        j++;
                        if (lineList.get(j).equals("  },")) break;
                    }
                    break;
                }
            }
        }
        if(dataBlock.length() == 0) {
            return null;
        }
        dataBlock.insert(0, " { ");
        return dataBlock.toString();
    }

    public List<Script.ScriptMethod> parseScriptMethod(String content) {
        // get method content
        String methodContent = getScriptMethod(content);
        if(methodContent.length() == 0) {
            return null;
        }

        // extract methods
        List<Script.ScriptMethod> methodList = new ArrayList<>();
        String ans = extractMethod(methodContent);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = JSONObject.parseArray(ans);
            for(int i = 0;i < jsonArray.size();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                methodList.add(new Script.ScriptMethod(jsonObject.getString("name"), jsonObject.getJSONArray("params").toJavaList(String.class), jsonObject.getString("content")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("script method json format error:\n" + ans);
        }
        return methodList;
    }

    private String extractMethod(String content) {
        String prompt = """
                给定下面的代码内容，你的任务是对其进行解析返回一个Method的列表。Method是一个类，属性包含：
                    String类型的name,
                    List<String>类型的params,
                    String类型的content
                
                下面是给出的代码片段:
                {content}
                
                请你返回一个json格式表示的Method对象列表，格式如下所示：
                [
                    {
                         "name": "",
                        "params": [],
                        "content": ""
                    }
                ]
                """;
        prompt = prompt.replace("{content}", content);
        String answer = llmGenerateService.generateAnswer(prompt);
        if(Objects.isNull(answer)) {
            return null;
        }
        if(answer.contains("```json")) {
            answer = answer.substring(answer.indexOf("```json") + 7, answer.lastIndexOf("```"));
        }
        return answer;
    }

    private String getScriptMethod(String content) {
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("  methods:")) {
                int j = i + 1;
                while(j < lineList.size()) {
                    dataBlock.append(lineList.get(j));
                    j++;
                    if(lineList.get(j).equals("  },") || lineList.get(j).equals("  }")) break;
                }
                break;
            }
        }
        return dataBlock.toString();
    }

    public static void main(String[] args) {
        String str = """
                {
                  "talk": {
                    "id": null,
                    "content": "",
                    "isTop": 0,
                    "status": 1,
                    "images": ""
                  },
                  "statuses": [
                    {
                      "status": 1,
                      "desc": "公开"
                    },
                    {
                      "status": 2,
                      "desc": "私密"
                    }
                  ],
                  "ups": [],
                  "headers": {
                    "Authorization": "Bearer " + sessionStorage.getItem('token')
                  }
                }
                """;
//        JSONObject jsonObject = JSONObject.parseObject(str);
//        jsonObject.forEach((k, v) -> {
//            System.out.println(k + ": " + v);
//        });
        Map<String, String> m = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        m.put("a", "1"); jsonObject.put("a", "1");
        m.put("b", "2"); jsonObject.put("b", "2");
        System.out.println(m.toString());
        System.out.println(jsonObject.toJSONString());
    }

}
