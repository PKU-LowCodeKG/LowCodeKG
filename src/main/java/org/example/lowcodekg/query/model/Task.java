package org.example.lowcodekg.query.model;

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
@Getter
@Setter
public class Task {

    private Long taskId;

    private String description;

    private List<DSL> dslList = new ArrayList<>();

    // Getter and Setter methods
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DSL> getDslList() {
        return dslList;
    }

    public void setDslList(List<DSL> dslList) {
        this.dslList = dslList;
    }
}
