package org.example.lowcodekg.query.service.processor.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.micrometer.common.util.StringUtils;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.lowcodekg.query.utils.Constants.MAX_RESOURCE_RECOMMEND_NUM;
import static org.example.lowcodekg.query.utils.Prompt.FILTER_BY_DEPENDENCY_PROMPT;

/**
 * @author Sherloque
 * @Date 2025/3/26
 */
@Service
public class TaskMatchingImpl implements TaskMatching {

    @Autowired
    private IRGenerate irGenerate;
    @Autowired
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private DebugConfig debugConfig;

    @Override
    public Result<Void> rerankResource(Task task) {
        try {
            // 类别路由的资源检索策略
            List<Node> nodeList = templateRetrieve.queryBySubTask(task).getData();
            if(debugConfig.isDebugMode()) {
                System.out.println("类别路由策略检索结果:\n" + nodeList.size() + "\n");
                System.out.println("类别路由策略检索资源:\n" + nodeList + "\n");
            }
            Map<Node, Double> nodeScoreMap = new HashMap<>();

            // 根据任务上下游依赖进行初步过滤
//            nodeList = filterByDependency(task, nodeList);
//            task.setResourceList(nodeList);
//            if(debugConfig.isDebugMode()) {
//                System.out.println("依赖关系过滤后资源个数:\n" + nodeList.size() + "\n");
//                System.out.println("根据依赖关系过滤后资源:\n" + nodeList + "\n");
//            }

            // 相似度计算
            for(Node node : nodeList) {
                Result<Double> scoreResult = subTaskMatchingScore(task, node);
                if(scoreResult.getCode() == ResultCodeEnum.SUCCESS.getCode()) {
                    nodeScoreMap.put(node, scoreResult.getData());
                }
            }
            // 根据资源与任务相似度进行重排序(升序)
            List<Node> sortedNodeList = nodeScoreMap.entrySet()
                    .stream()
                    .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                    .map(Map.Entry::getKey)
                    .limit(MAX_RESOURCE_RECOMMEND_NUM)
                    .collect(Collectors.toList());

            // 更新任务资源列表
            task.setResourceList(sortedNodeList);
            if(debugConfig.isDebugMode()) {
                System.out.println("重排序后的资源:\n" + sortedNodeList + "\n");
            }

            return Result.build(null, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while reranking resources: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error occurred while reranking resources: " + e.getMessage());
        }
    }

    @Override
    public Result<Double> subTaskMatchingScore(Task task, Node node) {
        try {
            // 获取task对应的IR序列
            List<IR> taskIRList = irGenerate.convertTaskToIR(task).getData();
            // 获取node对应的IR序列
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
            double transCost = minTransformCost(taskVectorList, templateVectorList);

            return Result.build(transCost, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error occurred while calculating subTaskMatchingScore: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error occurred while calculating subTaskMatchingScore: " + e.getMessage());
        }
    }

    /**
     * 根据任务上下游依赖进行检索资源列表的初步过滤
     * @param task
     * @param nodeList
     */
    private List<Node> filterByDependency(Task task, List<Node> nodeList) {
        try {
            String upstreamDependency = StringUtils.isBlank(task.getUpstreamDependency()) ? "null" : task.getUpstreamDependency();
            String downstreamDependency = StringUtils.isBlank(task.getDownstreamDependency()) ? "null" : task.getDownstreamDependency();
            // no dependency -> return all nodes
            if(StringUtils.isEmpty(upstreamDependency) && StringUtils.isEmpty(downstreamDependency)) {
                return nodeList;
            }

            String taskInfo = task.getName() + "\n" + task.getDescription();
            StringBuilder nodeInfos = new StringBuilder();
            for(Node node: nodeList) {
                nodeInfos.append(node.toString()).append("\n");
            }

            String prompt = FILTER_BY_DEPENDENCY_PROMPT
                    .replace("{task}", taskInfo)
                    .replace("{upstreamDependency}", upstreamDependency)
                    .replace("{downstreamDependency}", downstreamDependency)
                    .replace("{nodeList}", nodeInfos.toString());
            if(debugConfig.isDebugMode()) {
                System.out.println("根据依赖关系过滤prompt:\n" + prompt + "\n");
            }
            String answer = FormatUtil.extractJson(llmService.generateAnswer(prompt));
            if(debugConfig.isDebugMode()) {
                System.out.println("根据依赖关系过滤prompt:\n" + prompt + "\n");
                System.out.println("根据依赖关系过滤资源:\n" + answer + "\n");
            }

            JSONObject jsonObject = JSONObject.parseObject(answer);
            JSONArray jsonArray = jsonObject.getJSONArray("resources");
            Set<String> reservedNames = new HashSet<>();

            for(int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String taskName = item.getString("name");
                reservedNames.add(taskName);
            }

            List<Node> filteredNodeList = nodeList.stream()
                    .filter(node -> reservedNames.contains(node.getName()))
                    .collect(Collectors.toList());
            nodeList.clear();

            return filteredNodeList;

        } catch (Exception e) {
            System.err.println("Error occurred while filtering by dependency: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error occurred while filtering by dependency: " + e.getMessage());
        }
    }

    /**
     * 序列2转换为序列1的最低成本
     * @param vectorList1
     * @param vectorList2
     * @return
     */
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
        return dp[m][n] / m;
    }
}
