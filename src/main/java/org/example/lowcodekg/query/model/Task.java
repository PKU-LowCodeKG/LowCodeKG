package org.example.lowcodekg.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 用户需求分解后的子任务模型
 * @Author Sherloque
 * @Date 2025/3/21 19:52
 */
@Data
@AllArgsConstructor
public class Task {

    private String id;

    private String name;

    private String description;

    private List<DSL> dslList;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task: {")
                .append("id='").append(id).append('\'')
                .append(", name='").append(name).append('\'')
                .append(", description='").append(description).append('\'')
                .append(", dslList=[");

        for (int i = 0; i < dslList.size(); i++) {
            DSL dsl = dslList.get(i);
            sb.append(dsl.toString());
            if (i < dslList.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]}");
        return sb.toString();
    }
}
