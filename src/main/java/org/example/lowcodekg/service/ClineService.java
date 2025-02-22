package org.example.lowcodekg.service;


import org.example.lowcodekg.model.vo.Result;

public interface ClineService {

    /**
     * 返回项目功能架构摘要
     */
    Result<String> getProjectSummarization(String projectPath);

    /**
     * 基于用户给定功能需求，生成功能实现代码，同时完成文件更新
     */
    Result<String> responseUserRequirement(String requirement);

}
