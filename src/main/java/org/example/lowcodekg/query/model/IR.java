package org.example.lowcodekg.query.model;

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
        return "在" + condition + "的条件下，针对" + object + "执行" + action + "的操作，得到" + target + "的结果。";
    }
}
