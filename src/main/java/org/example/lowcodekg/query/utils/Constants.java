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
    public static final int MAX_DATA_OBJECT_NUM = 10;
    public static final int MAX_PAGE_NUM = 10;
    public static final int MAX_WORKFLOW_NUM = 10;
    public static final float MIN_SCORE = 0.6f;

    /**
     * 子任务推荐候选资源重排序后的个数
     */
    public static final int MAX_RESOURCE_RECOMMEND_NUM = 7;

}
