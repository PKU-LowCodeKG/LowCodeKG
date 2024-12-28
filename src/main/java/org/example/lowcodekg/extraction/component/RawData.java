package org.example.lowcodekg.extraction.component;

import lombok.Getter;
import lombok.Setter;
import org.example.lowcodekg.schema.entity.category.Category;
import org.example.lowcodekg.schema.entity.page.Component;
import org.example.lowcodekg.schema.entity.page.ConfigItem;

import java.util.ArrayList;
import java.util.List;

public class RawData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String name_CN;

    @Getter
    @Setter
    private String category;

    @Getter
    @Setter
    private String sceneLabel;

    @Getter
    @Setter
    private String description = "";

    @Getter
    @Setter
    private String usage = "";

    @Getter
    private ArrayList<CodeDemo> codeDemos = new ArrayList<CodeDemo>();

    @Getter
    private ArrayList<RawConfigItem> configItems = new ArrayList<RawConfigItem>();

    // Test function for RawData
    public void Test() {
        System.out.println("name = " + name);
        System.out.println("name_CN = " + name_CN);
        System.out.println("category = " + category);
        System.out.println("sceneLabel = " + sceneLabel);
        System.out.println("description = " + description);
        System.out.println("usage = " + usage);
        for (int i = 0; i < codeDemos.size(); i++) {
            codeDemos.get(i).Test();
        }
        for (int i = 0; i < configItems.size(); i++) {
            configItems.get(i).Test();
        }
    }

    public Component convertToComponent() {
        Component component = new Component();
        component.setName(name);
        component.setCategory(Category.setCategoryBy(category));
        component.setDescription(description + "\n" + usage);

        List<ConfigItem> configItemList = new ArrayList<ConfigItem>();
        for(RawConfigItem configItem: configItems) {
            configItemList.add(configItem.convertToConfigItem());
        }
        component.setContainedConfigItems(configItemList);
        return component;
    }
}
