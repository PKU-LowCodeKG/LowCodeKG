package org.example.lowcodekg.query.service.ir;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;

import java.util.List;

/**
 * @Description 将自然语言描述转化为功能原语表示
 * @Author Sherloque
 * @Date 2025/3/22 20:48
 */
public interface IRGenerate {

    /**
     * 构建阶段调用
     * @param description
     * @return
     */
    Result<List<IR>> generateIR(String description, String type);

    /**
     * 将需求描述（子任务）转化为中间表示
     *
     * @param task LLM分解后的子任务
     * @return
     */
    Result<List<IR>> convertTaskToIR(Task task);

    /**
     * 将模板资源描述转化为中间表示
     * @param template
     * @return
     */
    Result<List<IR>> convertTemplateToIR(Node template);

    /**
     * 将LLM输出的答案转化为IR(功能类服务)
     * @param answer
     * @return
     */
    List<IR> buildIRList(String answer);

    /**
     * 计算两个IR的相似度
     * @param ir1
     * @param ir2
     * @return
     */
    Double calculateIRSim(IR ir1, IR ir2);
}
