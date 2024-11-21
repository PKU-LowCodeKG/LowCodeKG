package org.example.lowcodekg.schema.entity;

import lombok.Data;
import org.apache.shiro.util.CollectionUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.example.lowcodekg.schema.constant.DevMode;
import org.example.lowcodekg.schema.constant.FrameWorkType;
import org.example.lowcodekg.schema.constant.FrameworkType;
import org.example.lowcodekg.schema.constant.FunctionalityCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;

import java.util.Collections;
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
     * 框架类别
     */
    private List<FrameworkType> frameWorkTypes;

    /**
     * 开发模式
     */
    private List<DevMode> devModes;


    public static Category setCategoryBy(String str) {
        Category category = new Category();
        category.setFunctionalityCategories(CollectionUtils.asList(FunctionalityCategory.setByCode(str)));
        category.setSceneLabels(CollectionUtils.asList(SceneLabel.setByCode(str)));
        return category;
    }

    public String toString() {
        // TODO: 判空的处理
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
