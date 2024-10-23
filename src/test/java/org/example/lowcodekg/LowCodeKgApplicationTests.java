package org.example.lowcodekg;

import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.ConfigItemRepo;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
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

        ComponentEntity component = new ComponentEntity(
                 "单选框",
                 ComponentCategory.UI_COMPONENT,
                 SceneLabel.ECOMMERCE,
                 "用于在多个备选项中选中单个状态。");
        ConfigItemEntity configItem1 = new ConfigItemEntity(
                "autoFocus",
                "boolean",
                "false",
                "自动获取焦点");
        ConfigItemEntity configItem2 = new ConfigItemEntity(
                "checked",
                "boolean",
                "false",
                "指定当前是否选中"
        );

        component.getContainedConfigItems().add(configItem1);
        component.getContainedConfigItems().add(configItem2);
        System.out.println("目前component的配置项有： " + component.getContainedConfigItems());

        componentRepo.save(component);
        configItemRepo.saveAll(component.getContainedConfigItems());

        List<ComponentEntity> components = componentRepo.findByNameContaining("单选");
        System.out.println("查询名字包含[单选]的ComponentEntity: " + components);

        List<ConfigItemEntity> configItems = componentRepo.findConfigItemsByComponentName("单选框");
        System.out.println("查询名字为[单选框]的组件包含的ConfigItemEntity: " + configItems);
    }

}
