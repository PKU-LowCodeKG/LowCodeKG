package org.example.lowcodekg.model.schema.entity.page;

import lombok.Data;
import org.example.lowcodekg.model.dao.neo4j.entity.page.ConfigItemEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.ConfigItemRepo;

/**
 * 组件配置
 */
@Data
public class ConfigItem {

    /**
     * 配置项标识符
     */
    private String code;

    /**
     * 配置项类型
     */
    private String type;

    /**
     * 默认值
     */
    private String value;

    /**
     * 配置项描述
     */
    private String description;

    public ConfigItem() {}

    public ConfigItem(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public ConfigItem(ConfigItemEntity entity) {
        this.code = entity.getName();
        this.type = entity.getType();
        this.value = entity.getValue();
        this.description = entity.getDescription();
    }

    public ConfigItemEntity createConfigItemEntity(ConfigItemRepo configItemRepo) {
        ConfigItemEntity entity = new ConfigItemEntity();
        entity.setName(code);
        entity.setValue(value);
        entity = configItemRepo.save(entity);
        return entity;
    }

    public ConfigItemEntity storeInNeo4j(ConfigItemRepo configItemRepo) {
        ConfigItemEntity configEntity = new ConfigItemEntity();
        configEntity.setName(code);
        configEntity.setType(type);
        configEntity.setValue(value);
        configEntity.setDescription(description);
        return configItemRepo.save(configEntity);
    }
}