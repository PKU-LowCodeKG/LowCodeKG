package org.example.lowcodekg.query.utils;

/**
 * @Description 定义项目内常量
 * @Author Sherloque
 * @Date 2025/3/23 21:02
 */
public final class Constants {

    /**
     * ES 索引名称
     */
    public static final String PAGE_INDEX_NAME = "page";
    public static final String WORKFLOW_INDEX_NAME = "workflow";
    public static final String DATA_OBJECT_INDEX_NAME = "data_object";

    /**
     * 任务检索参数设置
     */
    // 初步检索
    public static final int MAX_RESULTS = 10;
    // 子任务检索
    public static final int MAX_DATA_OBJECT_NUM = 15;
    public static final int MAX_PAGE_NUM = 15;
    public static final int MAX_WORKFLOW_NUM = 15;
    public static final float MIN_SCORE = 0.9f;

    /**
     * 子任务推荐候选资源重排序后的个数
     */
    public static final int MAX_RESOURCE_RECOMMEND_NUM = 7;

    /**
     * 数据记录路径
     */
    public static final String GROUND_TRUTH_JSON_FILE_PATH = "D:\\Master\\Data\\Dataset\\ground_truth.json";
    public static final String saveResultPath = "D:\\Master\\Data\\Dataset\\result.json";
    public static final String BLOG_RESULT_PATH = "D:\\Master\\Logs\\blog_result.txt";
    public static final String logFilePath = "D:\\Master\\log.txt";

}
