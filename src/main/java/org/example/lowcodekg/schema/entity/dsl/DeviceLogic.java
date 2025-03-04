package org.example.lowcodekg.schema.entity.dsl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceLogic {
    @JsonProperty("projectUuid")
    private String projectUuid;

    @JsonProperty("appUuid")
    private String appUuid;

    @JsonProperty("nodeType")
    private String nodeType;

    @JsonProperty("cascadeDelete")
    private boolean cascadeDelete;

    @JsonProperty("serial")
    private int serial;

    @JsonProperty("parentUuid")
    private String parentUuid;

    @JsonProperty("referenceUuid")
    private String referenceUuid;

    @JsonProperty("document")
    private Object document; // It can be a Map<String, Object> or any other type based on actual data

    @JsonProperty("dependencyUuid")
    private String dependencyUuid;

    @JsonProperty("detail")
    private Detail detail;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        @JsonProperty("id")
        private int id;

        @JsonProperty("uuid")
        private String uuid;

        @JsonProperty("createdAt")
        private String createdAt;

        @JsonProperty("updatedAt")
        private String updatedAt;

        @JsonProperty("isDeleted")
        private boolean isDeleted;

        @JsonProperty("name")
        private String name;

        @JsonProperty("cnName")
        private String cnName;

        @JsonProperty("description")
        private String description;

        @JsonProperty("identifier")
        private String identifier;

        @JsonProperty("flowUuid")
        private String flowUuid;

        @JsonProperty("type")
        private String type;

        @JsonProperty("path")
        private List<String> path;

        @JsonProperty("attrs")
        private Map<String, Object> attrs;

        @JsonProperty("parentUuid")
        private String parentUuid;

        @JsonProperty("variableUuid")
        private String variableUuid;
    }
}
