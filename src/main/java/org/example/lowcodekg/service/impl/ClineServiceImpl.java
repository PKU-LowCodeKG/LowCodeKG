package org.example.lowcodekg.service.impl;

import org.example.lowcodekg.service.ClineService;
import org.springframework.stereotype.Service;

@Service
public class ClineServiceImpl implements ClineService {

    @Override
    public String getProjectSummarization(String projectPath)
    {
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
}
