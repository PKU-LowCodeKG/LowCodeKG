package org.example.lowcodekg.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.catalina.User;
import org.example.lowcodekg.model.vo.Result;
import org.example.lowcodekg.model.vo.ResultCodeEnum;
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
    public Result<String> getProjectSummarization(String projectPath) {
        /**
         * TODO: 之后需要根据workflowModule实体生成项目功能架构树
         */

        String res = """
                以下是该项目的功能架构
                
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
        return Result.build(res, ResultCodeEnum.SUCCESS);
    }

    @Override
    public Result<String> responseUserRequirement(String requirement) {
        // target code retrieval
        // already implemented functionalities
        StringBuilder implementedFunc = new StringBuilder();
        implementedFunc.append("""
                1. 用户注册
                2. 博客文章发布
                """);
        // relevant code files
        // NOTE:检索相关代码可能需要将整个文件内容完整给出，还有对于和需求开发隐形相关的文件，仅根据用户需求进行检索很难完全覆盖到
        JSONArray relevantFileList = new JSONArray();
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

        // construct prompt and get result from LLM
        String prompt = MessageFormat.format("""
                You are a programming expert, currently working on system development based on an existing software project. The user wishes to implement the following functionality: 
                {0}
                
                The existing functionalities already implemented in the software project include: 
                {1}
                
                The code implementing the above functionalities in the existing software project is as follows, including the file names, system paths, and specific code snippets: 
                {2}
                
                To achieve the user's development requirements, which files need to be created or modified? Please provide a specific solution. Your response must strictly follow the execute_command rule:
                ## execute_command
                Parameters:
                - command: (required) The CLI command to execute. This should be valid for the current operating system. Ensure the command is properly formatted and does not contain any harmful instructions.
                - requires_approval: (required) A boolean indicating whether this command requires explicit user approval before execution in case the user has auto-approve mode enabled. Set to 'true' for potentially impactful operations like installing/uninstalling packages, deleting/overwriting files, system configuration changes, network operations, or any commands that could have unintended side effects. Set to 'false' for safe operations like reading files/directories, running development servers, building projects, and other non-destructive operations.
                Usage:
                <execute_command>
                <command>Your command here</command>
                <requires_approval>true or false</requires_approval>
                </execute_command>
                
                ## write_to_file
                Description: Request to write content to a file at the specified path. If the file exists, it will be overwritten with the provided content. If the file doesn't exist, it will be created. This tool will automatically create any directories needed to write the file.
                Parameters:
                - path: (required) The path of the file to write to
                - content: (required) The content to write to the file. ALWAYS provide the COMPLETE intended content of the file, without any truncation or omissions. You MUST include ALL parts of the file, even if they haven't been modified.
                Usage:
                <write_to_file>
                <path>File path here</path>
                <content>
                Your file content here
                </content>
                </write_to_file>
                
                ## replace_in_file
                Description: Request to replace sections of content in an existing file using SEARCH/REPLACE blocks that define exact changes to specific parts of the file. This tool should be used when you need to make targeted changes to specific parts of a file.
                Parameters:
                - path: (required) The path of the file to modify
                - diff: (required) One or more SEARCH/REPLACE blocks following this exact format:
                  \\`\\`\\`
                  <<<<<<< SEARCH
                  [exact content to find]
                  =======
                  [new content to replace with]
                  >>>>>>> REPLACE
                  \\`\\`\\`
                  Critical rules:
                  1. SEARCH content must match the associated file section to find EXACTLY:
                     * Match character-for-character including whitespace, indentation, line endings
                     * Include all comments, docstrings, etc.
                  2. SEARCH/REPLACE blocks will ONLY replace the first match occurrence.
                     * Including multiple unique SEARCH/REPLACE blocks if you need to make multiple changes.
                     * Include *just* enough lines in each SEARCH section to uniquely match each set of lines that need to change.
                     * When using multiple SEARCH/REPLACE blocks, list them in the order they appear in the file.
                  3. Keep SEARCH/REPLACE blocks concise:
                     * Break large SEARCH/REPLACE blocks into a series of smaller blocks that each change a small portion of the file.
                     * Include just the changing lines, and a few surrounding lines if needed for uniqueness.
                     * Do not include long runs of unchanging lines in SEARCH/REPLACE blocks.
                     * Each line must be complete. Never truncate lines mid-way through as this can cause matching failures.
                  4. Special operations:
                     * To move code: Use two SEARCH/REPLACE blocks (one to delete from original + one to insert at new location)
                     * To delete code: Use empty REPLACE section
                Usage:
                <replace_in_file>
                <path>File path here</path>
                <diff>
                Search and replace blocks here
                </diff>
                </replace_in_file>
                """, requirement, implementedFunc.toString(), relevantFileList.toString());
        String result = llmGenerateService.generateAnswer(prompt);
        if(result.startsWith("```json")) {
            result = result.substring(8, result.length() - 3);
        }
        return Result.build(result, ResultCodeEnum.SUCCESS);

        // format transform for different tools

//        try {
//            JSONArray jsonArray = JSON.parseArray(result);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            System.err.println("LLM generate error");
//        }

        // edit file

    }
}
