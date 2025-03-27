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

    private Double minTransformCost(List<float[]> vectorList1, List<float[]> vectorList2) {
        int m = vectorList1.size(); // vectorList1 的长度
        int n = vectorList2.size(); // vectorList2 的长度

        // dp[i][j] 表示将 vectorList1 的前 i 个向量转换为 vectorList2 的前 j 个向量的最小成本
        double[][] dp = new double[m + 1][n + 1];

        // 初始化边界条件
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i; // 删除 vectorList1 中的 i 个向量
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j; // 在 vectorList1 中新增 j 个向量
        }

        // 动态规划填表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // 计算相似度
                double similarity = EmbeddingUtil.cosineSimilarity(vectorList1.get(i - 1), vectorList2.get(j - 1));

                // 计算修改成本
                double modifyCost = 1 - similarity;

                // 选择最小成本的操作
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, // 删除 vectorList1[i-1]
                                dp[i][j - 1] + 1), // 新增 vectorList2[j-1]
                        dp[i - 1][j - 1] + modifyCost // 修改 vectorList1[i-1] 为 vectorList2[j-1]
                );
            }
        }

        // 返回最终的最小成本
        return dp[m][n];
    }
}
