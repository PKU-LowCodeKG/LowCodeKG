package org.example.lowcodekg.schema.entity.page;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Script {

    @Data
    @AllArgsConstructor
    public static class ScriptMethod {
        private String name;
        private List<String> params;
        private String content;
    }

    @Data
    @AllArgsConstructor
    public static class ImportsComponent {
        private String name;
        private String path;
    }

    private String name;

    private String description;

    private String content;

    private JSONObject dataList;

    private List<ScriptMethod> methodList;

    private List<ImportsComponent> importsComponentList;

}


