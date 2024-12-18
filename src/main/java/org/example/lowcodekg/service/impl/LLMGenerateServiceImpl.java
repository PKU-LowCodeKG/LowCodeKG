package org.example.lowcodekg.service.impl;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.dto.Neo4jNode;
import org.example.lowcodekg.service.LLMGenerateService;
import org.example.lowcodekg.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMGenerateServiceImpl implements LLMGenerateService {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    private String generateAnswer(String prompt) {
        UserMessage userMessage = UserMessage.from(prompt);
        AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
        return aiMessage.text();
    }

    @Override
    public String graphPromptToCode(String query, List<Neo4jNode> nodes) {

//        String template = """
//                你是一名程序员，现在正在使用springboot框架开发一个个人博客系统的后端。
//                当前，你要实现的功能为【{query}】。
//                请提供实现此功能的完整代码，包括以下部分：
//                1. 数据对象定义。创建实体类，定义相关字段。
//                2. 数据访问层（mapper或repository）。实现相应的接口和数据访问方法。
//                3. 服务层（service）。创建服务接口和实现类，包含相关的业务逻辑。
//                4. 控制器层（controller）。编写RESTful API接口，处理HTTP请求。
//                5. 其他可能用到的工具类等。
//
//                在编写代码之前，你在网络上搜索到了一些可能与该功能相关的代码片段，
//                请参考借鉴这些代码片段，最终完整地实现【{query}】功能。
//
//                请注意：在生成代码时，遵循最佳实践，代码结构清晰，并确保可以直接运行而无需过多调整。
//                请勿输出任何解释性文字，仅提供所需的代码。
//
//                可参考的代码:
//                {codeSamples}
//
//                你编写的代码：
//                """;
        String template = """
                你是一名程序员，现在正在使用springboot框架开发一个个人博客系统的后端。
                当前，你要实现的功能为【{query}】。
                该功能涉及项目内的若干文件，每个文件都有待补全的位置，已经用#TODO进行了醒目标注。
                
                在编写代码之前，你在网络上搜索到了一些可能与该功能相关的代码片段，
                请参考借鉴这些代码片段，对项目代码进行补全，最终完整地实现【{query}】功能。
                
                【注意】：项目内需要补全的多个文件已经提供给你，只需要补全#TODO标注的部分，其他部分不需要任何改动！
                【注意】：请勿输出任何解释性文字，仅提供所需的代码。
                
                网络上可参考的代码:
                {codeSamples}
                
                你需要补全的所有代码：
                {context}
                """;

        Map<String, Object> argumentMap = new HashMap<>();
        StringBuilder codeSamples = new StringBuilder();
        for (Neo4jNode neo4jNode : nodes) {
            // 忽略JavaField，因为JavaField没有content属性
            if (neo4jNode.getLabel().equals("JavaField")) {
                continue;
            }
            codeSamples.append(neo4jNode.getProperties().get("content"));
            codeSamples.append("\n\n");
        }

        String contextFilename = "/src/main/resources/data/code_context_examples/" + query + ".txt";
        String projectDir = System.getProperty("user.dir");
        File contextFile = new File(projectDir + "/" + contextFilename);
        String context = FileUtils.readFile(contextFile.getAbsolutePath());
        argumentMap.put("query", query);
        argumentMap.put("codeSamples", codeSamples.toString());
        argumentMap.put("context", context);
        String prompt = StrUtil.format(template, argumentMap);

        System.out.println(prompt);
        String answer = generateAnswer(prompt);

        String regex = "```[a-zA-Z0-9]+\\s*(.*?)```";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(answer);
        return matcher.replaceAll("$1");
    }
}
