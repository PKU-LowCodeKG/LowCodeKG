package org.example.lowcodekg.extraction.page;

import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.schema.entity.Component;
import org.example.lowcodekg.schema.entity.ConfigItem;
import org.example.lowcodekg.schema.entity.PageTemplate;
import org.example.lowcodekg.util.FileUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 前端页面抽取
 * 目前只实现对Vue框架的解析
 */
public class PageExtractor extends KnowledgeExtractor {

    @Override
    public void extraction() {
        for(String filePath: this.getDataDir()) {
            Collection<File> vueFiles = FileUtils.listFiles(new File(filePath), new String[]{"vue"}, true);
            for(File vueFile: vueFiles) {
                // 每个.vue文件解析为一个 PageTemplate 实体
                PageTemplate pageTemplate = new PageTemplate();
                pageTemplate.setName(vueFile.getName());
                String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

                // parse template
                String templateContent = getTemplateContent(fileContent);
                Document document = Jsoup.parse(templateContent);
                Element divElement = document.selectFirst("Template");
                divElement.children().forEach(element -> {
                    Component component = parseTemplate(element, null);
                    pageTemplate.getComponentList().add(component);
                });

                // parse script
                String scriptContent = getScriptContent(fileContent);


                // neo4j store


            }
        }
    }

    private String getTemplateContent(String fileContent) {
        Pattern pattern = Pattern.compile("<template>(.*?)</template>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(0).trim(); // 返回去除前后空白的模板内容
        } else {
            return null;
        }
    }

    private String getScriptContent(String fileContent) {
        Pattern pattern = Pattern.compile("<script>(.*?)</script>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(1).trim(); // 返回去除前后空白的脚本内容
        } else {
            return null;
        }
    }

    private static Component parseTemplate(Element element, Element parent) {
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

    public static void main(String[] args) {
        String content = """
                <template>
                  <div class="login-container">
                    <div class="login-card">
                      <el-form status-icon :model="loginForm" :rules="rules" ref="ruleForm" class="login-form">
                        <el-form-item prop="username">
                          <el-input
                            v-model="loginForm.username"
                            prefix-icon="el-icon-user-solid"
                            placeholder="用户名"
                            @keyup.enter.native="login" />
                        </el-form-item>
                        <el-form-item prop="password">
                          <el-input
                            v-model="loginForm.password"
                            prefix-icon="iconfont el-icon-mymima"
                            show-password
                            placeholder="密码"
                            @keyup.enter.native="login" />
                        </el-form-item>
                      </el-form>
                      <el-button type="primary" @click="login">登录</el-button>
                    </div>
                  </div>
                  <div class="login-title">管理员登录</div>
                </template>
                """;
        Document document = Jsoup.parse((content));
        Element divElement = document.selectFirst("template");
        PageTemplate pageTemplate = new PageTemplate();
        divElement.children().forEach(element -> {
            pageTemplate.getComponentList().add(parseTemplate(element, null));
//            System.out.println(element.toString());
//            System.out.println("-----------------------------");
        });
        System.out.println(pageTemplate.getComponentList().size());
    }

}
