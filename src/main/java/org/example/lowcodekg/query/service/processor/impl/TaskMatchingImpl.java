package org.example.lowcodekg.query.service.processor.impl;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.utils.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sherloque
 * @Date 2025/3/26
 */
@Service
public class TaskMatchingImpl implements TaskMatching {

    @Autowired
    private IRGenerate irGenerate;

    @Override
    public Result<Double> subTaskMatchingScore(Task task, Node node) {
        try {
            // 获取task和node对应的IR序列
            List<IR> taskIRList = irGenerate.convertTaskToIR(task).getData();
            List<IR> templateIRList = irGenerate.convertTemplateToIR(node).getData();

            // 序列向量化表示
            List<float[]> taskVectorList = taskIRList.stream()
                    .map(ir -> ir.toSentence())
                    .map(EmbeddingUtil::embedText)
                    .map(FormatUtil::ListToArray).toList();
            List<float[]> templateVectorList = templateIRList.stream()
                    .map(ir -> ir.toSentence())
                    .map(EmbeddingUtil::embedText)
                    .map(FormatUtil::ListToArray).toList();

            // 基于DP计算序列转换成本


        } catch (Exception e) {
            System.err.println("Error occurred while calculating subTaskMatchingScore: " + e.getMessage());
            throw new RuntimeException("Error occurred while calculating subTaskMatchingScore: " + e.getMessage());
        }
        return null;
    }

}
