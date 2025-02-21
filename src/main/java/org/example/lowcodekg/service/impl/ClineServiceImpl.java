package org.example.lowcodekg.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.catalina.User;
import org.example.lowcodekg.service.ClineService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
public class ClineServiceImpl implements ClineService {

    @Autowired
    private LLMGenerateService llmGenerateService;

    @Override
    public String getProjectSummarization(String projectPath) {
        /**
         * TODO: 之后需要根据workflowModule实体生成项目功能架构树
         */

        return """
                博客系统
                ├── 用户管理
                │   ├── 修改账户信息 [1]
                ├── 博客管理
                │   ├── 博客列表查询 [2]
                │   ├── 获取博客详情 [6]
                │   ├── 删除博客 [3]
                │   ├── 更新博客可见性 [5]
                ├── 分类与标签管理
                │   ├── 获取分类和标签 [4]
                ├── 评论管理
                │   ├── 分页查询评论 [7]
                │   ├── 更新评论公开状态 [8]
                │   ├── 删除评论 [9]
                │   ├── 修改评论 [10]
                ├── 友链管理
                │   ├── 获取友链列表 [11]
                │   ├── 更新友链公开状态 [12]
                │   ├── 添加友链 [13]
                ├── 动态管理
                │   ├── 获取动态列表 [14]
                │   ├── 更新动态公开状态 [15]
                ├── 访客管理
                │   ├── 获取访客列表 [16]
                │   ├── 删除访客 [17]
                """;
    }

    @Override
    public void responseUserRequirement(String requirement) {
        // target code retrieval
        JSONArray relevantFileList = new JSONArray();
        StringBuilder implementedFunc = new StringBuilder();
        // mock data
        implementedFunc.append("""
                1. 用户注册
                2. 博客文章发布
                """);
        try {
            JSONObject file1 = new JSONObject();
            file1.put("fileName", "/src/main/java/org/example/lowcodekg/service/UserService.java");
            file1.put("filePath", "/Users/chang/Documents/projects/LowCodeKG//src/main/java/org/example/lowcodekg/service/UserService.java");
            file1.put("fileContent", "public class UserService { private int a; }");

            relevantFileList.add(file1);
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("code retrieval error");
        }

        // generate code
        String prompt = MessageFormat.format("""
                你是一个编程专家，目前用户基于一个已有的软件项目进行系统开发，用户希望实现的功能是：
                {0}
                
                目前现有的软件项目已经实现的功能包括:
                {1}
                
                现有软件项目实现以上功能的代码如下，包含了相关代码所属文件的名称、系统路径以及具体的代码片段:
                {2}
                
                为了实现用户的开发需求，需要在此基础上进行哪些修改，请给出具体的解决方案，你的返回结果必须严格按照如下json格式：
                [
                    {
                        "filePath": "", // 待修改文件的全局路径
                        "editType": "", // 对当前文件的修改类型，包含 add, modify, delete 三种操作类型
                        "elementType": "", // 待修改的代码元素的类型，对于Java语言，包含 class, method, filed 三种类型
                        "srcContent": "", // 修改前的代码内容，如果 editType 是 add，则为空
                        "dstContent": "" // 修改后的代码内容，如果 editType 是 delete，则为空
                    }
                ]
                """, requirement, implementedFunc.toString(), relevantFileList.toString());
        String result = llmGenerateService.generateAnswer(prompt);
        if(result.startsWith("```json")) {
            result = result.substring(8, result.length() - 3);
        }
        // format transform for different tools
        try {
            JSONArray jsonArray = JSON.parseArray(result);
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("LLM generate error");
        }

        // edit file

    }


}
