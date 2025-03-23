package org.example.lowcodekg.query.service.decomposer;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.service.ElasticSearchService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:55
 */
@Service
public class TaskSplitImpl implements TaskSplit {
    @Autowired
    private LLMGenerateService llmService;

    @Override
    public Result<TaskGraph> taskSplit(String query) {

        // 根据需求检索相关资源


        // 基于检索结果，构造提示让LLM进行任务分解

        // 基于分解后的任务，让LLM判断子任务之间的依赖约束关系，形成子任务依赖图

        return null;
    }
}
