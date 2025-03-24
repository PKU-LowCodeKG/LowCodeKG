package org.example.lowcodekg.query.service.ir;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.DSL;
import org.example.lowcodekg.query.model.Task;

import java.util.List;

/**
 * @Description 将自然语言描述转化为功能原语表示
 * @Author Sherloque
 * @Date 2025/3/22 20:48
 */
public interface DslGenerate {

    /**
     * 将需求描述（子任务）转化为功能原语表示
     *
     * @param task LLM分解后的子任务
     * @return
     */
    Result<List<DSL>> convertTaskToIR(Task task);

    /**
     * 将模板资源描述转化为功能原语表示
     * @param template
     * @return
     */
    Result<List<DSL>> convertTemplateToIR(String template);
}
