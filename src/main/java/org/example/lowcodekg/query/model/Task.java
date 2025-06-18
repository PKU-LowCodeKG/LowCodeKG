package org.example.lowcodekg.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
     * 子任务转换得到的IR列表
     */
    private List<IR> irList;

    /**
     * 子任务推荐的资源列表
     */
    private List<Node> resourceList;

    /**
     * 任务的上下游依赖
     */
    private String upstreamDependency;
    private String downstreamDependency;


    public Task(String id, String name, List<String> category, String description, List<IR> irList) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.irList = irList;
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
