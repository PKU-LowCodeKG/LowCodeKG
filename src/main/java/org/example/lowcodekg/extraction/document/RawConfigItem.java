package org.example.lowcodekg.extraction.document;

import lombok.Getter;
import lombok.Setter;
import org.example.lowcodekg.model.schema.entity.page.ConfigItem;

public class RawConfigItem {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private String defaultValue;

    @Getter
    @Setter
    private String version;

    // Test function for RawConfigItem
    public void Test() {
        System.out.println("name = " + name);
        System.out.println("description = " + description);
        System.out.println("type = " + type);
        System.out.println("defaultValue = " + defaultValue);
        System.out.println("version = " + version);
    }

    public ConfigItem convertToConfigItem() {
        ConfigItem configItem = new ConfigItem();
        configItem.setCode(name);
        configItem.setType(type);
        configItem.setValue(defaultValue);
        configItem.setDescription(description);
        return configItem;
    }
}
