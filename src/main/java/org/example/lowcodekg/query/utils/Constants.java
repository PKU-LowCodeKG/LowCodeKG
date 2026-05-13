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
    public static final String CODE_ENTITY_INDEX_NAME = "code_entity";
    public static final String SUBTASK_INDEX_NAME = "subtask";

    /**
     * 任务检索参数设置
     */
    // 初步检索
    public static final int MAX_RESULTS = 10;
    // 子任务检索
    public static final int MAX_CODE_ENTITY_NUM = 30;
    public static final int MAX_SUBTASK_NUM = 10;
    public static final float MIN_SCORE = 0.9f;

    /**
     * 子任务推荐候选资源重排序后的个数
     */
    public static final int MAX_RESOURCE_RECOMMEND_NUM = 1;

    /**
     * 数据记录路径
     */
    public static final String BLOG_GROUND_TRUTH_JSON_FILE_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\blog_ground_truth.json";
    public static final String SAVE_BLOG_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\blog_output.json";
    public static final String BLOG_EVALUATE_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\blog_result.txt";

    public static final String EM_GROUND_TRUTH_JSON_FILE_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\em_ground_truth.json";
    public static final String SAVE_EM_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\em_output.json";
    public static final String EM_EVALUATE_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\em_result.txt";

    public static final String logFilePath = "D:\\Master\\log.txt";

}
