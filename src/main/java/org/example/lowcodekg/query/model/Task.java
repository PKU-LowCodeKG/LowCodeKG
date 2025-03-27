package org.example.lowcodekg.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;

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

    /**
     * 子任务转换得到的IR列表
     */
    private List<IR> irList;

    /**
     * 子任务推荐的资源列表
     */
    private List<Node> resourceList;


    public Task(String id, String name, String description, List<IR> irList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.irList = irList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task: {")
                .append("id='").append(id).append('\'')
                .append(", name='").append(name).append('\'')
                .append(", description='").append(description).append('\'')
                .append(", dslList=[");

        for (int i = 0; i < irList.size(); i++) {
            IR dsl = irList.get(i);
            sb.append(dsl.toString());
            if (i < irList.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]}");
        return sb.toString();
    }
}
