package org.example.lowcodekg.query.service.evaluation;

import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.service.processor.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 执行实验验证方法效果
 */
@Service
public class Evaluate {

    @Autowired
    private MainService mainService;
    @Autowired
    private Metric metric;

    /**
     * 执行评估并返回整体评估结果
     * @return double[] 包含[平均准确率, 平均召回率]
     */
    public double[] evaluate() {
        try {
            // load ground truth
            Map<String, List<String>> groundTruth = DataProcess.getQueryResultMap();
            
            double totalPrecision = 0.0;
            double totalRecall = 0.0;
            int validQueryCount = 0;

            // run experiment
            for(String query: groundTruth.keySet()) {
                try {
                    // 获取预测结果
                    List<Node> predictedNodes = mainService.recommend(query).getData();
                    List<String> predicted = predictedNodes.stream()
                            .map(Node::getFullName)
                            .toList();
                    
                    // 获取真实结果
                    List<String> groundTruthResult = groundTruth.get(query);
                    
                    // 计算当前查询的指标
                    double precision = metric.calculatePrecision(predicted, groundTruthResult);
                    double recall = metric.calculateRecall(predicted, groundTruthResult);
                    
                    // 累加结果
                    totalPrecision += precision;
                    totalRecall += recall;
                    validQueryCount++;

                    // 打印每个查询的评估结果
                    System.out.printf("Query: %s\nPrecision: %.4f, Recall: %.4f\n", 
                            query, precision, recall);
                    System.out.println("Predicted: " + predicted);
                    System.out.println("Ground Truth: " + groundTruthResult);
                    System.out.println("----------------------------------------");

                } catch (Exception e) {
                    System.err.println("Error in evaluate query: " + query);
                    e.printStackTrace();
                    // 继续执行下一个查询，而不是直接抛出异常
                }
            }

            // 计算平均值
            if (validQueryCount > 0) {
                double avgPrecision = totalPrecision / validQueryCount;
                double avgRecall = totalRecall / validQueryCount;
                
                // 打印总体评估结果
                System.out.println("\n=== Overall Evaluation Results ===");
                System.out.printf("Average Precision: %.4f\n", avgPrecision);
                System.out.printf("Average Recall: %.4f\n", avgRecall);
                System.out.printf("Total Valid Queries: %d\n", validQueryCount);
                
                return new double[]{avgPrecision, avgRecall};
            }

            return new double[]{0.0, 0.0};

        } catch (Exception e) {
            System.err.println("Error in evaluate: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in evaluate: " + e.getMessage());
        }
    }
}
