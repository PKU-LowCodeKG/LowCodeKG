package org.example.lowcodekg.query.service.decomposer;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.Task;
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
    public Result<List<Task>> taskSplit(String query) {

        return null;
    }
}
