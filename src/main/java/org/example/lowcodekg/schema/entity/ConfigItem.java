package org.example.lowcodekg.schema.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;
import org.example.lowcodekg.dao.neo4j.repository.ConfigItemRepo;
import org.neo4j.graphdb.Label;

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
    private String defaultValue;

    /**
     * 配置项描述
     */
    private String description;

    public ConfigItem() {}

    public ConfigItem(ConfigItemEntity entity) {
        this.code = entity.getCode();
        this.type = entity.getType();
        this.defaultValue = entity.getDefaultValue();
        this.description = entity.getDescription();
    }

    public ConfigItemEntity storeInNeo4j(ConfigItemRepo configItemRepo) {
        ConfigItemEntity configEntity = new ConfigItemEntity();
        configEntity.setCode(code);
        configEntity.setType(type);
        configEntity.setDefaultValue(defaultValue);
        configEntity.setDescription(description);
        return configItemRepo.save(configEntity);
    }
}