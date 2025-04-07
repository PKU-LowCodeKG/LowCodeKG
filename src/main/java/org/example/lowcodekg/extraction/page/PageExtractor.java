package org.example.lowcodekg.extraction.page;

import com.alibaba.fastjson.JSONObject;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.model.dao.neo4j.entity.page.*;
import org.example.lowcodekg.model.schema.entity.page.Component;
import org.example.lowcodekg.model.schema.entity.page.ConfigItem;
import org.example.lowcodekg.model.schema.entity.page.PageTemplate;
import org.example.lowcodekg.model.schema.entity.page.Script;
import org.example.lowcodekg.common.util.FileUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.lowcodekg.common.util.PageParserUtil.*;


/**
 * 前端页面抽取
 * 目前只实现对Vue框架的解析
 */
@Service
public class PageExtractor extends KnowledgeExtractor {

    private Map<String, PageEntity> pageEntityMap = new HashMap<>();
    private Map<String, PageTemplate> pageTemplateMap = new HashMap<>();

    /**
     * single file record data structure
     */
    private Map<String, ScriptMethodEntity> scriptMethodMap = new HashMap<>();
    private Map<String, ConfigItemEntity> configItemMap = new HashMap<>();

    @Override
    public void extraction() {
        for(String filePath: this.getDataDir()) {
            // 获取项目名称
            String projectName = filePath.substring(filePath.lastIndexOf("\\") + 1);
            filePath = filePath.replace("\\\\", "\\");
            
            Collection<File> vueFiles = FileUtils.listFiles(new File(filePath), new String[]{"vue"}, true);
            for(File vueFile: vueFiles) {
                System.out.println("---parse file: " + vueFile.getAbsolutePath());
                // initialize
                scriptMethodMap.clear();
                configItemMap.clear();

                PageTemplate pageTemplate = new PageTemplate();
                String name = vueFile.getName().substring(0, vueFile.getName().length()-4);
                String relativePath = vueFile.getAbsolutePath()
                        .replace("\\\\", "\\")
                        .replace(filePath.substring(0, filePath.indexOf(projectName)), "");
                String fullName = relativePath
                        .replace("\\", ".")
                        .replaceAll("\\.vue$", "");
                pageTemplate.setName(name);
                pageTemplate.setFullName(fullName);
                String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

                // for test
//                if(!name.equals("FriendList")) {
//                    continue;
//                }

                // parse template
                String templateContent = getTemplateContent(fileContent);
                if(StringUtils.isNotEmpty(templateContent)) {
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
                // create neo4j entities
                storeNeo4j(pageTemplate);
                // create relations
                parseRelBetweenConfigItemAndScriptMethod();
            }
            // create relationships among page entities
            parseRelations();
        }
    }

    /**
     * store page-related entities and relationships in neo4j
     */
    public PageEntity storeNeo4j(PageTemplate pageTemplate) {
        try {
            PageEntity pageEntity = pageTemplate.createPageEntity(pageRepo);
            pageEntityMap.put(pageEntity.getName(), pageEntity);
            pageTemplateMap.put(pageEntity.getName(), pageTemplate);
            // component entity
            for(Component component: pageTemplate.getComponentList()) {
                ComponentEntity componentEntity = createComponentEntity(component);
                pageEntity.getComponentList().add(componentEntity);
                pageRepo.createRelationOfContainedComponent(pageEntity.getId(), componentEntity.getId());
            }
            // script entity
            if(!Objects.isNull(pageTemplate.getScript())) {
                Script script = pageTemplate.getScript();
                ScriptEntity scriptEntity = script.createScriptEntity(scriptRepo);
                pageRepo.createRelationOfContainedScript(pageEntity.getId(), scriptEntity.getId());
                // script method
                List<ScriptMethodEntity> scriptMethodEntityList = script.createScriptMethodEntityList(scriptMethodRepo);
                for (ScriptMethodEntity scriptMethodEntity : scriptMethodEntityList) {
                    scriptMethodMap.put(scriptMethodEntity.getName(), scriptMethodEntity);
                    scriptRepo.createRelationOfContainedMethod(scriptEntity.getId(), scriptMethodEntity.getId());
                }
                // script data
                List<ScriptDataEntity> scriptDataEntityList = script.createScriptDataEntityList(scriptDataRepo);
                for (ScriptDataEntity scriptDataEntity : scriptDataEntityList) {
                    scriptRepo.createRelationOfContainedData(scriptEntity.getId(), scriptDataEntity.getId());
                }
            }
            return pageEntity;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in PageExtractor storeNeo4j: " + e.getMessage());
            return null;
        }
    }

    /**
     * parse relationships between page entities
     */
    private void parseRelations() {
        // page-[dependency]->page
        try {
            pageEntityMap.values().forEach(pageEntity -> {
                PageTemplate pageTemplate = pageTemplateMap.get(pageEntity.getName());
                if(!Objects.isNull(pageTemplate.getScript())) {
                    pageTemplate.findDependedPage();
                    pageTemplate.getDependedPageList().forEach(dependedPageName -> {
                        if(pageEntityMap.containsKey(dependedPageName)) {
                            pageRepo.createRelationOfDependedPage(pageEntity.getId(), pageEntityMap.get(dependedPageName).getId());
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in parseRelations: " + e.getMessage());
        }
    }

    private ComponentEntity createComponentEntity(Component component) {
        ComponentEntity componentEntity = component.createComponentEntity(componentRepo);
        componentRepo.setComponentExample(componentEntity.getId());
        for(ConfigItem configItem: component.getConfigItemList()) {
            ConfigItemEntity configItemEntity = configItem.createConfigItemEntity(configItemRepo);
            componentEntity.getContainedConfigItemEntities().add(configItemEntity);
            componentRepo.createRelationOfContainedConfigItem(componentEntity.getId(), configItemEntity.getId());
            // generate unique key for config item
            String configItemKey = component.getName() + configItemEntity.getName() + configItemEntity.getValue();
            configItemMap.put(configItemKey, configItemEntity);
        }
        if(!Objects.isNull(component.getChildren())) {
            for (Component child : component.getChildren()) {
                ComponentEntity childComponentEntity = createComponentEntity(child);
                componentRepo.setComponentExample(childComponentEntity.getId());
                componentEntity.getChildComponentList().add(childComponentEntity);
                componentRepo.createRelationOfChildComponent(componentEntity.getId(), childComponentEntity.getId());
            }
        }
        return componentEntity;
    }

    private void parseRelBetweenConfigItemAndScriptMethod() {
        // configItem-[related_to]->scriptMethod
        configItemMap.values().forEach(configItemEntity -> {
            String value = configItemEntity.getValue();
            Pattern p = Pattern.compile("(\\w+)\\(([\\w,:\\s=\\.]*)\\)");
            Matcher match = p.matcher(value);
            if(match.find()) {
                String name = match.group(1);
                if (scriptMethodMap.containsKey(name)) {
                    ScriptMethodEntity methodEntity = scriptMethodMap.get(name);
                    configItemRepo.createRelationOfRelatedMethod(configItemEntity.getId(), methodEntity.getId());
                }
            } else if(scriptMethodMap.containsKey(value)) {
                ScriptMethodEntity methodEntity = scriptMethodMap.get(value);
                configItemRepo.createRelationOfRelatedMethod(configItemEntity.getId(), methodEntity.getId());
            }
        });
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
        List<Script.ScriptData> dataList = parseScriptData(content);
        script.setDataList(dataList);

        // parse methods
        List<Script.ScriptMethod> methodList = parseScriptMethod(content);
        script.setMethodList(methodList);

        return script;
    }

    public List<Script.ScriptData> parseScriptData(String content) {
        // get data block
        String dataBlock = getScriptData(content);
        if(StringUtils.isEmpty(dataBlock)) {
            return null;
        }
        // json format
        String prompt = """
                给定下面的代码内容，你的任务是对其进行解析返回一个json对象，以 ```json 开始，以 ``` 结尾。注意，如果key对应的value包含了表达式或函数调用，将其转为字符串格式
                比如：对于
                "headers": {
                    "Authorization": "Bearer " + sessionStorage.getItem('token')
               }，应该表示为：
                "headers": {
                    "Authorization": "'Bearer ' + sessionStorage.getItem('token')"
                  }
                如果包含了"//""标识的注释内容，请将注释的文字删除
                
                下面是给出的代码片段:
                {content}
                """;
        List<Script.ScriptData> dataList = new ArrayList<>();
        try {
            prompt = prompt.replace("{content}", dataBlock);
            String answer = llmGenerateService.generateAnswer(prompt);
            if(answer.contains("```json")) {
                answer = answer.substring(answer.indexOf("```json") + 7, answer.lastIndexOf("```"));
            }
            JSONObject jsonObject = JSONObject.parseObject(answer);
            jsonObject.forEach((k, v) -> {
                Script.ScriptData data = new Script.ScriptData();
                data.setName(k);
                data.setValue(Objects.isNull(v) ? "null" : v.toString());
                dataList.add(data);
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("script data json format error:\n" + dataBlock);
        }
        return dataList;
    }

    public List<Script.ScriptMethod> parseScriptMethod(String content) {
        // get method content
        String methodContent = getScriptMethod(content);
        if(methodContent.length() == 0) {
            return null;
        }

        // extract methods
        List<Script.ScriptMethod> methodList = new ArrayList<>();
        List<String> lines = Arrays.asList(methodContent.split("\n"));
        String name = "";
        List<String> params;
        StringBuilder mContent = new StringBuilder();
        int i = 0;
        while(i < lines.size()) {
            String line = lines.get(i);
            Pattern p = Pattern.compile("(\\w+)\\(([\\w,:\\s=\\.]*)\\)\\s*\\{");
            Matcher match = p.matcher(line);
            if(match.find()) {
                name = match.group(1);
                params = Arrays.asList(match.group(2).split(", "));
                String intent = getScriptIndent(line);
                int j = i + 1;
                while(j < lines.size()) {
                    if(lines.get(j).equals(intent + "},")
                            || lines.get(j).equals(intent + "}")) {
                        break;
                    }
                    mContent.append(lines.get(j));
                    j++;
                }
                i = j;
                methodList.add(new Script.ScriptMethod(name, params, mContent.toString()));
                mContent = new StringBuilder();
            }
            i++;
        }
        return methodList;
    }
}
