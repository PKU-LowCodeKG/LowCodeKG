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
        String conditionPart = StringUtils.isBlank(condition) || condition.equals("null") ? "" : "在" + condition + "的条件下, ";
        String objectPart = StringUtils.isBlank(action) || action.equals("null") ? "对象是" + object : "针对" + object + " ";
        String actionPart = StringUtils.isBlank(action) || action.equals("null") ? "" : "执行" + action + "操作 ";
        String targetPart = StringUtils.isBlank(target) || target.equals("null") ? "" : ",得到" + target + "的结果";
        return conditionPart + objectPart + actionPart + targetPart;
    }
}
