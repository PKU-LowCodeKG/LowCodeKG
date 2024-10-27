package org.example.lowcodekg.schema.entity;

import lombok.Data;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.example.lowcodekg.schema.constant.FunctionalityCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;

/**
 * 低代码模板的分类信息，支持多种分类
 */
@Data
public class Category {

    /**
     * 功能类别
     */
    private FunctionalityCategory functionalityCategory;

    /**
     * 场景标签
     */
    private SceneLabel sceneLabel;

    public static Category setCategoryBy(String str) {
        Category category = new Category();
        category.setFunctionalityCategory(FunctionalityCategory.setByCode(str));
        category.setSceneLabel(SceneLabel.setByCode(str));
        return category;
    }

    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("functionalityCategory", functionalityCategory.getCode());
            json.put("sceneLabel", sceneLabel.getCode());
        } catch (JSONException e) {
            return "";
        }
        return json.toString();
    }
}
