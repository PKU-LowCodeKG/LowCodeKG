package org.example.lowcodekg.schema.entity.page;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptDataEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.ScriptDataRepo;
import org.example.lowcodekg.dao.neo4j.repository.ScriptMethodRepo;
import org.example.lowcodekg.dao.neo4j.repository.ScriptRepo;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Script {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScriptData {
        private String name;
        private String value;
        private String description;

        public ScriptDataEntity createScriptDataEntity(ScriptDataRepo scriptDataRepo) {
            ScriptDataEntity scriptDataEntity = new ScriptDataEntity();
            scriptDataEntity.setName(name);
            scriptDataEntity.setValue(value);
            scriptDataEntity.setDescription(description);
            scriptDataEntity = scriptDataRepo.save(scriptDataEntity);
            return scriptDataEntity;
        }
    }

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

    private List<ScriptData> dataList;

    private List<ScriptMethod> methodList = new ArrayList<>();

    private String importsComponentList;

    public ScriptEntity createScriptEntity(ScriptRepo scriptRepo, ScriptMethodRepo scriptMethodRepo, ScriptDataRepo scriptDataRepo) {
        ScriptEntity scriptEntity = new ScriptEntity();
        scriptEntity.setName(name);
        scriptEntity.setDescription(description);
        scriptEntity.setContent(content);
        if(!Objects.isNull(importsComponentList))
            scriptEntity.setImportsComponentList(importsComponentList);
        scriptEntity = scriptRepo.save(scriptEntity);

        // 创建 ScriptMethod 实体
        if(!Objects.isNull(methodList)) {
            for (ScriptMethod method : methodList) {
                ScriptMethodEntity methodEntity = new ScriptMethodEntity();
                methodEntity.setName(method.getName());
                methodEntity.setParams(method.getParams());
                methodEntity.setContent(method.getContent());
                methodEntity = scriptMethodRepo.save(methodEntity);
                scriptRepo.createRelationOfContainedMethod(scriptEntity.getId(), methodEntity.getId());
            }
        }
        // 创建 ScriptData 实体
        if(!Objects.isNull(dataList)) {
            for (ScriptData data : dataList) {
                ScriptDataEntity dataEntity = new ScriptDataEntity();
                dataEntity.setName(data.getName());
                dataEntity.setValue(data.getValue());
                dataEntity = scriptDataRepo.save(dataEntity);
                scriptRepo.createRelationOfContainedData(scriptEntity.getId(), dataEntity.getId());
            }
       }
        return scriptEntity;
    }

}