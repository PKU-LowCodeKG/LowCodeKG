package org.example.lowcodekg.query.model;

import lombok.Data;

/**
 * @Description 功能原语定义：<Action, Object, Target, Condition>
 * @Author Sherloque
 * @Date 2025/3/21 19:49
 */
@Data
public class DSL {

    private String action;

    private String object;

    private String target;

    private String condition;

}
