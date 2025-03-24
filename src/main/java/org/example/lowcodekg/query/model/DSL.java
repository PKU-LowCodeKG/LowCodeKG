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
public class DSL {

    private String action;

    private String object;

    private String target;

    private String condition;

}
