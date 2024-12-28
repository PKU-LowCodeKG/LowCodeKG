package org.example.lowcodekg.extraction.page;

import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
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
                PageTemplate pageTemplate = new PageTemplate();
                pageTemplate.setName(vueFile.getName());

                // read file content
                String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

                // template parse
                String templateContent = getTemplateContent(fileContent);

                // script parse
                String scriptContent = getScriptContent(fileContent);

            }
        }
    }

    private static String getTemplateContent(String fileContent) {
        Pattern pattern = Pattern.compile("<template>(.*?)</template>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(0).trim(); // 返回去除前后空白的模板内容
        } else {
            return null;
        }
    }

    private static String getScriptContent(String fileContent) {
        Pattern pattern = Pattern.compile("<script>(.*?)</script>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(1).trim(); // 返回去除前后空白的脚本内容
        } else {
            return null;
        }
    }

    private static void parseTemplate(Element element, Element parent) {
        System.out.println("元素标签: " + element.tagName());
        System.out.println("元素类名: " + element.className());
        System.out.println("元素文本: " + element.text());
        element.attributes().forEach(attr -> {
            System.out.println("属性名: " + attr.getKey() + ", 属性值: " + attr.getValue());
        });
        System.out.println("-----------------------------");
        for (Element child : element.children()) {
            parseTemplate(child, element);
        }
    }

    public static void main(String[] args) {
        String content = """
                <template>
                  <div class="login-container">
                    <div class="login-card">
                      <div class="login-title">管理员登录</div>
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
                </template>
                
                """;
        Document document = Jsoup.parse(getTemplateContent(content));
        Elements divElement = document.select("*");
        divElement.forEach(element -> {
            System.out.println(element.tagName());
            if (Objects.equals(element.tagName(), "div")) {
                parseTemplate(element, null);
            }
        });
    }

}
