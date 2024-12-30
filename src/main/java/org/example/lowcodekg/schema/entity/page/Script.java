package org.example.lowcodekg.schema.entity.page;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptEntity;
import org.example.lowcodekg.dao.neo4j.repository.ScriptRepo;

import java.util.List;
import java.util.Map;

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

    private String name;

    private String description;

    private String content;

    private JSONObject dataList;

    private List<ScriptMethod> methodList;

    private Map<String, String> importsComponentList;

    public ScriptEntity createScriptEntity(ScriptRepo scriptRepo) {
        ScriptEntity scriptEntity = new ScriptEntity();
        scriptEntity.setName(name);
        scriptEntity.setDescription(description);
        scriptEntity.setContent(content);
        scriptEntity.setDataList(dataList.toJSONString());
        scriptEntity.setImportsComponentList(importsComponentList);
        scriptEntity = scriptRepo.save(scriptEntity);
        return scriptEntity;
    }

}


