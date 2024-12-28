package org.example.lowcodekg.schema.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.PageEntity;
import org.example.lowcodekg.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.schema.entity.category.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端页面模板
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageTemplate {

    private String name;

    private String cnName;

    private String description;

    private String sourceCode;

    private Category category;

    /**
     * 页面包含组件列表
     */
    private List<Component> componentList = new ArrayList<>();

    /**
     * 页面配置项列表
     */
    private List<ConfigItem> configItemList = new ArrayList<>();

    /**
     * 页面脚本
     */



    public PageEntity storeInNeo4j(PageRepo pageRepo) {
        PageEntity pageEntity = new PageEntity();

        return pageEntity;
    }
}
