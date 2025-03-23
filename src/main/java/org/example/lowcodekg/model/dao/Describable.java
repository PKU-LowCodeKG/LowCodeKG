package org.example.lowcodekg.model.dao;

/**
 * 可描述实体的接口，用于统一处理具有名称和描述的实体
 */
public interface Describable {
    /**
     * 获取实体的唯一标识
     */
    Long getId();

    /**
     * 获取实体的名称
     */
    String getName();

    /**
     * 获取实体的描述
     */
    String getDescription();

    /**
     * 获取实体的类型
     */
    default String getEntityType() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}