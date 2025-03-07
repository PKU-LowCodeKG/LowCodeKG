package org.example.lowcodekg.model.schema.entity.category;

import lombok.Data;
import org.apache.shiro.util.CollectionUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 低代码模板的分类信息，支持多种分类
 */
@Data
public class Category {

    /**
     * 功能类别
     */
    private List<FunctionalityCategory> functionalityCategories;

    /**
     * 场景标签
     */
    private List<SceneLabel> sceneLabels;

    /**
     * 以下分类信息预先指定，暂不需要执行分类逻辑
     */

    /**
     * 框架类别
     */
    private List<FrameworkType> frameWorkTypes;

    /**
     * 开发模式
     */
    private List<DevMode> devModes;


    public static Category setCategoryBy(String str) {
        Category category = new Category();
        category.setFunctionalityCategories(FunctionalityCategory.setByCode(str));
        category.setSceneLabels(SceneLabel.setByCode(str));
        return category;
    }

    public String toString() {
        JSONObject json = new JSONObject();
        if(!CollectionUtils.isEmpty(functionalityCategories)) {
            List<String> codes = functionalityCategories.stream().map(FunctionalityCategory::getCode).collect(Collectors.toList());
            try {
                json.put("functionalityCategories", codes);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(!CollectionUtils.isEmpty(sceneLabels)) {
            List<String> codes = sceneLabels.stream().map(SceneLabel::getCode).collect(Collectors.toList());
            try {
                json.put("sceneLabels", codes);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }
}
