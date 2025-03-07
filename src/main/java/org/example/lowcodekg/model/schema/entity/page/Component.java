package org.example.lowcodekg.model.schema.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.neo4j.entity.page.ComponentEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.ConfigItemEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.ConfigItemRepo;
import org.example.lowcodekg.model.schema.entity.category.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 低代码组件实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Component {

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件中文名称
     */
    private String cnName;

    /**
     * 文本元素内容
     */
    private String text;

    private String content;

    /**
     * 组件类别，包括场景标签、功能分类等
     */
    private Category category;

    /**
     * 组件描述
     */
    private String description;

    /**
     * 组件子项(体现为template中的层级嵌套关系)
     */
    private List<Component> children = new ArrayList<>();

    /**
     * 组件关联的配置型
     */
    private List<ConfigItem> configItemList = new ArrayList<>();

    /**
     * 组件依赖的其他组件（outgoing）
     */
    private List<Component> relatedComponentList = new ArrayList<>();


    public Component(ComponentEntity entity) {
        this.name = entity.getName();
        this.category = Category.setCategoryBy(entity.getCategory());
        this.description = entity.getDescription();
        this.configItemList = entity.getContainedConfigItemEntities().stream().map(e -> new ConfigItem(e)).toList();
    }

    public ComponentEntity createComponentEntity(ComponentRepo componentRepo) {
        ComponentEntity entity = new ComponentEntity();
        entity.setName(name);
        entity.setText(text);
        entity.setContent(content);
        entity.setDescription(description);
        entity = componentRepo.save(entity);
        return entity;
    }

    /**
     * 将数据对象持久化到Neo4j
     */
    public ComponentEntity storeInNeo4j(ComponentRepo componentRepo, ConfigItemRepo configItemRepo) {
        try {
            // 设置组件实体属性
            ComponentEntity entity = new ComponentEntity();
            entity.setName(name);
            entity.setCategory(category.toString());
            entity.setDescription(description);

            // 组件关联的配置型列表
            List<ConfigItemEntity> configItemEntities = new ArrayList<>();
            for(ConfigItem configItem: configItemList) {
                // 存储配置项节点
                ConfigItemEntity configEntity = configItem.storeInNeo4j(configItemRepo);
                configItemEntities.add(configEntity);
            }
            entity.getContainedConfigItemEntities().addAll(configItemEntities);

            // Neo4j 存储数据对象
            entity = componentRepo.save(entity);
            configItemRepo.saveAll(entity.getContainedConfigItemEntities());
            return entity;

        } catch (Exception e) {
            System.err.println("Error in storeInNeo4j: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将数据对象持久化到ES
     */
    public void storeInES() {
        try {

        } catch (Exception e) {
            System.err.println("Error in storeInES: " + e.getMessage());
            e.printStackTrace();
        }
    }

}