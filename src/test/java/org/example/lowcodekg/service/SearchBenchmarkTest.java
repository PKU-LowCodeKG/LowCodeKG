package org.example.lowcodekg.service;

import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
public class SearchBenchmarkTest {

    @Autowired
    private TaskSplit taskSplit;
    @Autowired
    private TaskMatching taskMatching;
    @Autowired
    private TaskMerge taskMerge;

    private static final List<String> QUERIES = List.of(
            "实现博客文章置顶的功能",
            "实现博客评论审核的功能",
            "实现文章密码保护功能",
            "实现博客访问统计功能",
            "实现博客分类管理功能",
            "实现博客标签管理功能",
            "实现定时任务管理功能",
            "实现博客搜索功能",
            "实现博客文章编辑功能",
            "实现友链申请功能",
            "实现友链评论功能",
            "更改友链公开状态",
            "实现动态发布功能",
            "实现动态公开状态管理功能",
            "实现动态点赞功能",
            "实现用户登录功能",
            "实现登录日志记录功能",
            "实现用户密码修改功能",
            "实现关于我页面配置",
            "实现站点设置管理功能"
    );

    @Test
    void benchmark() {
        List<Long> stage1Times = new ArrayList<>();
        List<Long> stage2Times = new ArrayList<>();
        List<Long> stage3Times = new ArrayList<>();
        List<Long> totalTimes = new ArrayList<>();
        int success = 0;
        int fail = 0;

        System.out.println("\n========== Search Benchmark (20 queries) ==========\n");

        for (int i = 0; i < QUERIES.size(); i++) {
            String query = QUERIES.get(i);
            System.out.printf("[%2d/%2d] %s%n", i + 1, QUERIES.size(), query);

            try {
                // 阶段一：任务分解
                long t1 = System.nanoTime();
                TaskGraph taskGraph = taskSplit.taskSplit(query).getData();
                long t2 = System.nanoTime();
                long stage1 = (t2 - t1) / 1_000_000;

                // 阶段二：资源匹配
                taskMatching.rerankResources(new ArrayList<>(taskGraph.getTasks().values()));
                long t3 = System.nanoTime();
                long stage2 = (t3 - t2) / 1_000_000;

                // 阶段三：任务合并
                Map<Task, Set<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
                long t4 = System.nanoTime();
                long stage3 = (t4 - t3) / 1_000_000;

                long total = (t4 - t1) / 1_000_000;

                stage1Times.add(stage1);
                stage2Times.add(stage2);
                stage3Times.add(stage3);
                totalTimes.add(total);

                // 统计结果数
                int resourceCount = resourceList.values().stream().mapToInt(Set::size).sum();
                System.out.printf("     阶段一(任务分解): %dms | 阶段二(资源匹配): %dms | 阶段三(任务合并): %dms | 总耗时: %dms | 结果数: %d%n",
                        stage1, stage2, stage3, total, resourceCount);
                success++;
            } catch (Exception e) {
                fail++;
                System.out.printf("     FAILED: %s%n", e.getMessage());
            }
        }

        // 汇总统计
        System.out.println("\n========== Summary ==========");
        System.out.printf("成功: %d, 失败: %d%n", success, fail);

        if (!totalTimes.isEmpty()) {
            printStats("阶段一(任务分解)", stage1Times);
            printStats("阶段二(资源匹配)", stage2Times);
            printStats("阶段三(任务合并)", stage3Times);
            printStats("总耗时", totalTimes);

            long totalAll = totalTimes.stream().mapToLong(Long::longValue).sum();
            double avgAll = totalAll / (double) totalTimes.size();
            System.out.printf("%n全部 %d 个查询总耗时: %dms, 平均: %.1fms%n", totalTimes.size(), totalAll, avgAll);
        }
    }

    private static void printStats(String label, List<Long> times) {
        LongSummaryStatistics stats = times.stream().mapToLong(Long::longValue).summaryStatistics();
        System.out.printf("  %s: min=%dms, max=%dms, avg=%.1fms, p50=%dms, p95=%dms%n",
                label,
                stats.getMin(),
                stats.getMax(),
                stats.getAverage(),
                percentile(times, 50),
                percentile(times, 95));
    }

    private static long percentile(List<Long> values, double p) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(idx, 0));
    }
}
