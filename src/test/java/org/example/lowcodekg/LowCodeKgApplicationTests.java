package org.example.lowcodekg;

import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;
import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.ConfigItemRepo;
import org.example.lowcodekg.schema.entity.category.FunctionalityCategory;
import org.example.lowcodekg.schema.entity.category.SceneLabel;
import org.example.lowcodekg.schema.entity.category.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class LowCodeKgApplicationTests {

    @Autowired
    private ComponentRepo componentRepo;

    @Autowired
    private ConfigItemRepo configItemRepo;

    @Test
    void contextLoads() {
    }

    @Test
    void componentAddSearchTest() {
        // 测试前先删除所有节点（同时也会删除相应关系）
        componentRepo.deleteAll();
        configItemRepo.deleteAll();

        Category category = new Category();
        category.setFunctionalityCategory(FunctionalityCategory.FORM);
        category.setSceneLabel(SceneLabel.ECOMMERCE);

        ComponentEntity componentEntity = new ComponentEntity(
                 "单选框",
                 "component",
                 "用于在多个备选项中选中单个状态。");
        ConfigItemEntity configItemEntity1 = new ConfigItemEntity(
                "autoFocus",
                "boolean",
                "false",
                "自动获取焦点");
        ConfigItemEntity configItemEntity2 = new ConfigItemEntity(
                "checked",
                "boolean",
                "false",
                "指定当前是否选中"
        );

        componentEntity.getContainedConfigItemEntities().add(configItemEntity1);
        componentEntity.getContainedConfigItemEntities().add(configItemEntity2);
        System.out.println("目前component的配置项有： " + componentEntity.getContainedConfigItemEntities());

        componentRepo.save(componentEntity);
        configItemRepo.saveAll(componentEntity.getContainedConfigItemEntities());

        List<ComponentEntity> componentEntities = componentRepo.findByNameContaining("单选");
        System.out.println("查询名字包含[单选]的ComponentEntity: " + componentEntities);

        List<ConfigItemEntity> configItemEntities = configItemRepo.findConfigItemsByComponentName("单选框");
        System.out.println("查询名字为[单选框]的组件包含的ConfigItemEntity: " + configItemEntities);
    }

}
