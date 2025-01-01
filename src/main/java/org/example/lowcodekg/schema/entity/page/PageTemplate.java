package org.example.lowcodekg.schema.entity.page;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.schema.entity.category.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 前端页面模板
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageTemplate {

    private String name;

    private String fullName;

    private String description;

    private String content;

    private Category category;

    /**
     * 页面包含组件列表
     */
    private List<Component> componentList = new ArrayList<>();

    private List<String> dependedPageList = new ArrayList<>();

    /**
     * 页面配置项列表
     */
    private List<ConfigItem> configItemList = new ArrayList<>();

    /**
     * 页面脚本
     */
    private Script script;

    public PageEntity createPageEntity(PageRepo pageRepo) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setName(name);
        pageEntity.setFullName(fullName);
        pageEntity.setDescription(description);
        pageEntity.setContent(content);
        pageEntity = pageRepo.save(pageEntity);
        return pageEntity;
    }

    public void findDependedPage() {
        String imports = this.script.getImportsComponentList();
        if(Objects.isNull(imports)) {
            return;
        }
        JSONObject importsList = JSONObject.parseObject(imports);
        importsList.forEach((k, v) -> {
            String component = (String) k;
            String path = (String) v;
            if(path.contains("@")) {
                dependedPageList.add(component);
            }
        });
    }
}
