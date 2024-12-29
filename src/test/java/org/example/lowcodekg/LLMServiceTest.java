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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.example.lowcodekg.extraction.page.PageExtractor.*;

@SpringBootTest
public class LLMServiceTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    public void test() {
        String prompt = "What is the capital of France?";
        UserMessage userMessage = UserMessage.from(prompt);
        AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
        String answer = aiMessage.text();
        System.out.println(answer);
    }

    @Test
    public void testVueParser() {
        String content = """
                <script>
                import { generaMenu } from '@/assets/js/menu'
                export default {
                  data: function () {
                    return {
                      loginForm: {
                        username: '',
                        password: ''
                      },
                      isDelete: false,
                      current: 1,
                      size: 10,
                      count: 0,
                      rules: {
                        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
                        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
                      }
                    }
                  },
                  methods: {
                    cycleChange() {
                      if (this.radioValue == '3') {
                        this.$emit('update', 'year', this.cycleTotal)
                      }
                    },
                    averageChange(size, jobs) {
                      if (this.radioValue == '4') {
                        this.$emit('update', 'year', this.averageTotal)
                      }
                    },
                    checkboxChange(task) {
                      if (this.radioValue == '5') {
                        this.$emit('update', 'year', this.checkboxString)
                      }
                    },
                    login() {
                      this.$refs.ruleForm.validate((valid) => {
                        if (valid) {
                          const that = this
                          let param = new URLSearchParams()
                          param.append('username', that.loginForm.username)
                          param.append('password', that.loginForm.password)
                          that.axios.post('/api/users/login', param).then(({ data }) => {
                            if (data.flag) {
                              that.$store.commit('login', data.data)
                              generaMenu()
                              that.$message.success('登录成功')
                              that.$router.push({ path: '/' })
                            } else {
                              that.$message.error(data.message)
                            }
                          })
                        } else {
                          return false
                        }
                      })
                    }
                  }
                }
                </script>
                """;

        PageTemplate pageTemplate = new PageTemplate();
        // parse script
        String scriptContent = getScriptContent(content);
        Script script = new Script();
        script.setContent(scriptContent);

        // parse import components
        List<Script.ImportsComponent> importsList = parseImportsComponent(scriptContent);
        script.setImportsComponentList(importsList);

        // parse data
        JSONObject data = parseScriptData(scriptContent);
        script.setDataList(data);

        // parse methods
        List<Script.ScriptMethod> methodList = parseScriptMethod(scriptContent);
        script.setMethodList(methodList);
        for(Script.ScriptMethod method: methodList) {
            System.out.println(method.getName());
            for(String param: method.getParams()) {
                System.out.println(param);
            }
            System.out.println(method.getContent());
        }

        pageTemplate.setScript(script);
    }
}
