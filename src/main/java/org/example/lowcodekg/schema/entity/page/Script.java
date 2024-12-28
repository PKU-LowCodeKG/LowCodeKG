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

    private String name;

    private String description;

    private String content;

    private List<ScriptData> dataList;

    private List<ScriptMethod> methodList;

    private List<ImportsComponent> importsCompoentList;

}

class ScriptData {
    private String name;
    private String value;
    private List<String> rules;
}

class ScriptMethod {
    private String name;
    private List<String> params;
    private String content;
}

class ImportsComponent {
    private String name;
    private String path;
}
