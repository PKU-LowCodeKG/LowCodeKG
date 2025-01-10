package org.example.lowcodekg.extraction.page;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.dao.neo4j.entity.page.*;
import org.example.lowcodekg.dao.neo4j.repository.ScriptMethodRepo;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.schema.entity.page.*;
import org.example.lowcodekg.util.FileUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            Collection<File> vueFiles = FileUtils.listFiles(new File(filePath), new String[]{"vue"}, true);
            for(File vueFile: vueFiles) {
                System.out.println("---parse file: " + vueFile.getAbsolutePath());
                // initialize
                scriptMethodMap.clear();
                configItemMap.clear();

                // each .vue file parsed as one PageTemplate entity
                PageTemplate pageTemplate = new PageTemplate();
                String name = vueFile.getName().substring(0, vueFile.getName().length()-4);
                String fullName = vueFile.getAbsolutePath().replace(filePath, "");
                pageTemplate.setName(name);
                pageTemplate.setFullName(fullName);
                String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

                // for test
//                if(!name.equals("TalkList")) {
//                    continue;
//                }

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
            parseRelations();
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
                // config item entity
                for(ConfigItem configItem: component.getConfigItemList()) {
                    ConfigItemEntity configItemEntity = configItem.createConfigItemEntity(configItemRepo);
                    componentEntity.getContainedConfigItemEntities().add(configItemEntity);
                    componentRepo.createRelationOfRelatedConfigItem(componentEntity.getId(), configItemEntity.getId());
                    configItemMap.put(component.getName() + ":" + configItemEntity.getName(), configItemEntity);
                }
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
        // configItem-[related_to]->scriptMethod
        configItemMap.values().forEach(configItemEntity -> {
           String value = configItemEntity.getValue();
           if(scriptMethodMap.containsKey(value)) {
               ScriptMethodEntity methodEntity = scriptMethodMap.get(value);
               configItemRepo.createRelationOfRelatedMethod(configItemEntity.getId(), methodEntity.getId());
           }
        });
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
        List<Script.ScriptData> dataList = parseScriptData(content);
        script.setDataList(dataList);

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

    public List<Script.ScriptData> parseScriptData(String content) {
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

    public String getScriptData(String content) {
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("data")) {
                if((lineList.get(i).contains("data()") || lineList.get(i).contains("function"))
                        && i+1 < lineList.size() && lineList.get(i+1).contains("return {")) {
                    int j = i + 2;
                    String intent = getScriptIndent(lineList.get(i));
                    while (j < lineList.size()) {
                        dataBlock.append(lineList.get(j));
                        j++;
                        if (j == lineList.size() || lineList.get(j).equals(intent + "},") || lineList.get(j).equals(intent + "}")) break;
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

    private String getScriptMethod(String content) {
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("methods: {")) {
                int j = i + 1;
                String intent = getScriptIndent(lineList.get(i));
                while(j < lineList.size()) {
                    dataBlock.append(lineList.get(j) + "\n");
                    j++;
                    if(j >= lineList.size()
                            || lineList.get(j).equals(intent + "},")
                            || lineList.get(j).equals(intent + "}"))
                        break;
                }
                break;
            }
        }
        return dataBlock.toString();
    }

    private String getScriptIndent(String line) {
        StringBuilder indentation = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indentation.append(" ");
            } else if (c == '\t') {
                indentation.append('\t');
            } else {
                break;
            }
        }
        return indentation.toString();
    }

    public static void main(String[] args) {

    }

}
