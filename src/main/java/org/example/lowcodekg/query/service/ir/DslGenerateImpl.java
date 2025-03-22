package org.example.lowcodekg.query.service.ir;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.DSL;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 20:50
 */
@Service
public class DslGenerateImpl implements DslGenerate {

    @Autowired
    private LLMGenerateService llmService;

    @Override
    public Result<List<DSL>> convertNLToDSL(String query) {

        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Override
    public Result<List<DSL>> convertTemplateToDSL(String template) {
        return null;
    }
}
