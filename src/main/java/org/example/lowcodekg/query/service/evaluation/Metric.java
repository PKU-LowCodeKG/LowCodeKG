package org.example.lowcodekg.query.service.evaluation;

// 修改导入语句
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 衡量模板推荐结果的指标
 * @Author Sherloque
 * @Date 2025/3/21 19:50
 */
@Component
public class Metric {

    /**
     * 计算单个查询结果的准确率
     * @param predicted 预测结果
     * @param groundTruth 真实结果
     * @return 准确率
     */
    public double calculatePrecision(List<String> predicted, List<String> groundTruth) {
        if (CollectionUtils.isEmpty(predicted)) {
            return 0.0;
        }
        
        int matchCount = 0;
        for (String item : predicted) {
            if (groundTruth.contains(item)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / predicted.size();
    }

    /**
     * 计算单个查询结果的召回率
     * @param predicted 预测结果
     * @param groundTruth 真实结果
     * @return 召回率
     */
    public double calculateRecall(List<String> predicted, List<String> groundTruth) {
        if (CollectionUtils.isEmpty(groundTruth)) {
            return 0.0;
        }
        
        int matchCount = 0;
        for (String item : groundTruth) {
            if (predicted.contains(item)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / groundTruth.size();
    }
}
