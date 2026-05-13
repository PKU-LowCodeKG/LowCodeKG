package org.example.lowcodekg.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 用户需求分解后的子任务模型
 * @Author Sherloque
 * @Date 2025/3/21 19:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    private String id;

    private String name;

    private List<String> category;
    private Boolean isData = false;
    private Boolean isPage = false;
    private Boolean isWorkflow = false;

    private String description;

    /**
     * 子任务推荐的资源列表
     */
    private List<Node> resourceList;

    /**
     * 任务的上下游依赖
     */
    private String upstreamDependency;
    private String downstreamDependency;


    public Task(String id, String name, List<String> category, String description, List<Node> resourceList) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.resourceList = resourceList;
        for(String type: category) {
            if("data".equals(type)) {
                this.isData = true;
            } else if("page".equals(type)) {
                this.isPage = true;
            } else if("workflow".equals(type)) {
                this.isWorkflow = true;
            }
        }
    }

    public void setUpstreamDependency(String description) {
        if(StringUtils.isNotBlank(description) && !"null".equals(description)) {
            this.upstreamDependency += description;
        }
    }

    public void setDownstreamDependency(String description) {
        if(StringUtils.isNotBlank(description) && !"null".equals(description)) {
            this.downstreamDependency += description;
        }
    }

    @Override
    public String toString() {
        return "Task: {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
