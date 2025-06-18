package org.example.lowcodekg.service.impl;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.model.dto.Neo4jNode;
import org.example.lowcodekg.service.LLMGenerateService;
import org.example.lowcodekg.common.util.FileUtil;
import org.example.lowcodekg.common.util.FormatParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMGenerateServiceImpl implements LLMGenerateService {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Override
    public String generateAnswer(String prompt) {
        int maxRetries = 1; // 设定最大重试次数
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                UserMessage userMessage = UserMessage.from(prompt);
                AiMessage aiMessage = ollamaChatModel.generate(userMessage).content();
                return aiMessage.text();
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                System.out.println("generateAnswer failed, retrying...");
            }
        }
        return null;
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
        String context = FileUtil.readFile(contextFile.getAbsolutePath());
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

    @Override
    public List<Map<String, Object>> selectInitialNodes(String query,
                                                        List<Map<String, Object>> initialNodeProps) {
        String template = """
                你是一名程序员，现在正在使用springboot框架开发一个个人博客系统的后端。
                当前，你要实现的功能为【{query}】。
                
                在编写代码之前，你在网络上搜索到了一些可能与该功能相关的代码片段，都来自其他的博客系统。
                这些代码片段可能有实现业务逻辑用到的method，也可能有数据实体的class。
                但是这些代码片段不一定都对你有帮助，所以事先需要进行判断，仅保留与【{query}】功能相关的代码。
                
                输入包括每个代码片段的编号、所在路径，以及它实现功能的描述。
                【注意】：你需要判断这些代码与【{query}】功能是否相关，只输出相关的代码片段的编号！
                【注意】：你需要严格遵守输出格式，输出一个列表，例如：[1, 4, 5]
                【注意】：请勿输出任何解释性文字，务必遵守输出格式！
                
                可能的代码片段:
                {codeSamples}
                
                """;

        Map<String, Object> argumentMap = new HashMap<>();
        StringBuilder codeSamples = new StringBuilder();
        for (int i = 0; i < initialNodeProps.size(); i++) {
            Map<String, Object> nodeProps = initialNodeProps.get(i);
            codeSamples.append("代码片段编号：").append(i).append("\n");
            codeSamples.append("代码所在路径：").append(nodeProps.get("fullName")).append("\n");
            codeSamples.append("代码功能：");
            if (nodeProps.get("description") != null) {
                codeSamples.append(nodeProps.get("description")).append("\n");
            } else {
                codeSamples.append(nodeProps.get("name")).append("\n");
            }
            codeSamples.append("\n");
        }

        argumentMap.put("query", query);
        argumentMap.put("codeSamples", codeSamples.toString());
        String prompt = StrUtil.format(template, argumentMap);

        System.out.println("初始节点集合过滤：");
        System.out.println(prompt);
        String answer = generateAnswer(prompt);
        System.out.println(answer);
        List<Integer> remainIdx = FormatParseUtil.parseIntList(answer);
        System.out.println("初始解析保留：" + remainIdx);

        List<Map<String, Object> > remainNodeProps = new ArrayList<>();
        for (int idx : remainIdx) {
            remainNodeProps.add(initialNodeProps.get(idx));
        }
        return remainNodeProps;
    }

    @Override
    public List<Integer> selectExtendNode(String query,
                                      List<Map<String, Object>> extendNodeProps) {
        String template = """
                你是一名程序员，现在正在使用springboot框架开发一个个人博客系统的后端。
                当前，你要实现的功能为【{query}】。
                
                在编写代码之前，你在网络上搜索到了一些可能与该功能相关的代码片段，都来自其他的博客系统。
                这些代码片段不一定是直接实现业务逻辑的method，也不必须是数据实体的class，但很可能是实现该功能所需要的。
                请你判断哪些代码是实现【{query}】功能所需要的。
                
                输入包括每个代码片段的编号、所在路径，以及它实现功能的描述。
                【注意】：你需要判断这些代码与【{query}】功能是否相关，只输出相关的代码片段的编号！
                【注意】：你需要严格遵守输出格式，输出一个列表，例如：[1, 4, 5]
                【注意】：请勿输出任何解释性文字，务必遵守输出格式！
                
                可能的代码片段:
                {codeSamples}
                
                """;

        Map<String, Object> argumentMap = new HashMap<>();
        StringBuilder codeSamples = new StringBuilder();
        for (int i = 0; i < extendNodeProps.size(); i++) {
            Map<String, Object> nodeProps = extendNodeProps.get(i);
            codeSamples.append("代码片段编号：").append(i).append("\n");
            codeSamples.append("代码所在路径：").append(nodeProps.get("fullName")).append("\n");
            codeSamples.append("代码功能：");
            if (nodeProps.get("description") != null) {
                codeSamples.append(nodeProps.get("description")).append("\n");
            } else {
                codeSamples.append(nodeProps.get("name")).append("\n");
            }
            codeSamples.append("\n");
        }

        argumentMap.put("query", query);
        argumentMap.put("codeSamples", codeSamples.toString());
        String prompt = StrUtil.format(template, argumentMap);

        System.out.println("扩展节点集合过滤：");
        System.out.println(prompt);
        String answer = generateAnswer(prompt);
        System.out.println(answer);
        List<Integer> remainIdx = FormatParseUtil.parseIntList(answer);
        System.out.println("扩展解析保留：" + remainIdx);

        return remainIdx;
    }

    @Override
    public List<Integer> selectRelevantFields(String query,
                                              String className,
                                              List<Map<String, Object>> fieldsProps) {
        String template = """
                你是一名程序员，现在正在使用springboot框架开发一个个人博客系统的后端。
                当前，你要实现的功能为【{query}】，首先需要定义数据类。
                                
                在编写代码之前，你在网络上搜索到了一些可能与该功能相关的代码片段，都来自其他的个人博客系统。
                其中一段代码是数据实体的class，里面定义了若干成员变量field。
                现在你需要判断这段代码中哪些field是更加重要的，是真正对【{query}】功能的开发具有帮助的。
                
                输入包含class 【{className}】 中每个field的编号、类型、名字。
                【注意】：你需要判断这些field是否对开发【{query}】功能有帮助，只输出重要的field的编号！数量尽量限制在5个以内！
                【注意】：你需要严格遵守输出格式，输出一个列表，例如：[1, 4, 5]
                【注意】：请勿输出任何解释性文字，务必遵守输出格式！
                
                {className}的成员变量:
                {fieldDetails}
                
                """;


        Map<String, Object> argumentMap = new HashMap<>();
        StringBuilder fieldDetails = new StringBuilder();
        for (int i = 0; i < fieldsProps.size(); i++) {
            Map<String, Object> fieldProp = fieldsProps.get(i);
            fieldDetails.append("field编号").append(i).append("：");
            fieldDetails.append(fieldProp.get("type")).append(" ");
            fieldDetails.append(fieldProp.get("name")).append("\n");
            fieldDetails.append("\n");
        }

        argumentMap.put("query", query);
        argumentMap.put("className", className);
        argumentMap.put("fieldDetails", fieldDetails.toString());
        String prompt = StrUtil.format(template, argumentMap);

        System.out.println("成员变量筛选：");
        System.out.println(prompt);
        String answer = generateAnswer(prompt);
        System.out.println(answer);
        List<Integer> relevantFieldIdx = FormatParseUtil.parseIntList(answer);
        System.out.println("扩展解析保留：" + relevantFieldIdx);

        return relevantFieldIdx;
    }

}
