package org.example.lowcodekg.query.model;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 功能原语定义：<Action, Object, Target, Condition>
 * @Author Sherloque
 * @Date 2025/3/21 19:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IR {

    private String action;
    private String object;
    private String target;
    private String condition;

    // 额外记录类型:Workflow, DataObject, PageTemplate
    private String type;

    public IR(IR ir) {
        this.action = ir.action;
        this.object = ir.object;
        this.target = ir.target;
        this.condition = ir.condition;
        this.type = ir.type;
    }

    @Override
    public String toString() {
        return "{" +
                "action='" + action + '\'' +
                ", object='" + object + '\'' +
                ", target='" + target + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }

    public String toSentence() {
//        if(StringUtils.isNotBlank(type) && ("PageTemplate".equals(type) || "DataObject".equals(type))) {
//            irStr = object + " " + condition;
//        } else {
//            String conditionPart = StringUtils.isBlank(condition) || condition.equals("null") ? "" : "在" + condition + "的条件下, ";
//            String objectPart = StringUtils.isBlank(action) || action.equals("null") ? "对象是" + object : "针对" + object + " ";
//            String actionPart = StringUtils.isBlank(action) || action.equals("null") ? "" : "执行" + action + "操作 ";
//            String targetPart = StringUtils.isBlank(target) || target.equals("null") ? "" : ",得到" + target + "的结果";
//            irStr = conditionPart + objectPart + actionPart + targetPart;
//        }
        String conditionPart = StringUtils.isBlank(condition) || condition.equals("null") ? "" : condition;
        String actionPart = StringUtils.isBlank(action) || action.equals("null") ? "" : action;
        String objectPart = StringUtils.isBlank(object) || object.equals("null") ? "" : object;
        String targetPart = StringUtils.isBlank(target) || target.equals("null") ? "" : target;
        String irStr = conditionPart + " " + actionPart + " " + objectPart + " " + targetPart;
        return irStr;
    }
}
