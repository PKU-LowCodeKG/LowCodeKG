package org.example.lowcodekg.extraction.page;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
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

/**
 * 前端页面抽取
 * 目前只实现对Vue框架的解析
 */
@Service
public class PageExtractor extends KnowledgeExtractor {

    @Override
    public void extraction() {
        for(String filePath: this.getDataDir()) {
            Collection<File> vueFiles = FileUtils.listFiles(new File(filePath), new String[]{"vue"}, true);
            for(File vueFile: vueFiles) {

                System.out.println("---parse file: " + vueFile.getAbsolutePath());
                // 每个.vue文件解析为一个 PageTemplate 实体
                PageTemplate pageTemplate = new PageTemplate();
                String name = vueFile.getName().substring(0, vueFile.getName().length()-5);
                pageTemplate.setName(name);
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


            }
        }
    }

    public static String getTemplateContent(String fileContent) {
        Pattern pattern = Pattern.compile("<template>(.*?)</template>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(0).trim(); // 返回去除前后空白的模板内容
        } else {
            return null;
        }
    }

    public static String getScriptContent(String fileContent) {
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

    public static Component parseTemplate(Element element, Element parent) {
        Component component = new Component();
        component.setName(element.tagName());
        component.setText(element.text());
        component.setSourceCode(element.toString());
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

    public static Script parseScript(String content) {
        Script script = new Script();
        script.setContent(content);

        // parse import components
        List<Script.ImportsComponent> importsList = parseImportsComponent(content);
        script.setImportsComponentList(importsList);

        // parse data
        JSONObject data = parseScriptData(content);
        script.setDataList(data);

        // parse methods
        List<Script.ScriptMethod> methodList = parseScriptMethod(content);
        script.setMethodList(methodList);

        return script;
    }

    public static List<Script.ImportsComponent> parseImportsComponent(String content) {
        try {
            List<Script.ImportsComponent> importsList = new ArrayList<>();
            String importPattern = "import\\s*\\{?\\s*([\\w,\\s]+)\\s*\\}?\\s*from\\s*['\"]([^'\"]+)['\"]";
            Pattern pattern = Pattern.compile(importPattern);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String names = matcher.group(1).trim();
                String path = matcher.group(2).trim();

                String[] nameArray = names.split("\\s*,\\s*");
                for (String name : nameArray) {
                    Script.ImportsComponent importsComponent = new Script.ImportsComponent(name, path);
                    importsList.add(importsComponent);
                }
            }
            return importsList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject parseScriptData(String content) {
        // get data block
        String[] lines = content.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));
        StringBuilder dataBlock = new StringBuilder();
        for(int i = 0;i < lineList.size();i++) {
            if(lineList.get(i).contains("data") && i+1 < lineList.size() && lineList.get(i+1).contains("return")) {
                int j = i + 2;
                while(j < lineList.size()) {
                    dataBlock.append(lineList.get(j));
                    j++;
                    if(lineList.get(j).equals("  },")) break;
                }
                break;
            }
        }
        if(dataBlock.length() == 0) {
            return null;
        }

        // json format
        JSONObject jsonObject = new JSONObject();
        try {
            dataBlock.insert(0, "{ ");
           jsonObject = JSONObject.parseObject(dataBlock.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("script data json format error:\n" + dataBlock);
        }
        return jsonObject;
    }

    public static List<Script.ScriptMethod> parseScriptMethod(String content) {
        // get method content
        String methodContent = getScriptMethod(content);
        if(methodContent.length() == 0) {
            return null;
        }

        // extract methods
        List<Script.ScriptMethod> methodList = new ArrayList<>();
        String ans = extractMethod(methodContent);
        if(ans.startsWith("```json")) {
            ans = ans.substring(7, ans.length() - 3);
        }
        JSONArray jsonArray = JSONObject.parseArray(ans);
        for(int i = 0;i < jsonArray.size();i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            methodList.add(new Script.ScriptMethod(jsonObject.getString("name"), jsonObject.getJSONArray("params").toJavaList(String.class), jsonObject.getString("content")));
        }
        return methodList;
    }

    private static String extractMethod(String content) {
        String prompt = """
                给定下面的代码内容，你的任务是对其进行解析返回一个Method的列表。Method是一个类，属性包含：name, params, content
                例如对于以下代码片段：
                averageChange(cnt, res) {
                      if (this.radioValue == '4') {
                        this.$emit('update', 'year', this.averageTotal)
                      }
                    },
                解析得到的method对象属性为：
                    name：averageChange
                    参数列表：[cnt, res]
                    content：if (this.radioValue == '4') { this.$emit('update', 'year', this.averageTotal) }
                      
                下面是给出的代码片段:
                {content}
                
                请你返回一个json格式表示的Method对象列表
                """;
        prompt = prompt.replace("{content}", getScriptMethod(content));
        String answer = llmGenerateService.generateAnswer(prompt);
        return answer;
    }

    private static String getScriptMethod(String content) {
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
    }

}
