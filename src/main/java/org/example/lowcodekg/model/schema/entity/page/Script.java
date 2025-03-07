package org.example.lowcodekg.model.schema.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.model.dao.neo4j.entity.page.ScriptDataEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.ScriptEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.ScriptMethodEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.ScriptDataRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.ScriptMethodRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.ScriptRepo;

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

        public ScriptMethodEntity createScriptMethodEntity(ScriptMethodRepo scriptMethodRepo) {
            ScriptMethodEntity methodEntity = new ScriptMethodEntity();
            methodEntity.setName(name);
            methodEntity.setParams(params);
            methodEntity.setContent(content);
            methodEntity = scriptMethodRepo.save(methodEntity);
            return methodEntity;
        }
    }

    private String name;

    private String description;

    private String content;

    private List<ScriptData> dataList = new ArrayList<>();

    private List<ScriptMethod> methodList = new ArrayList<>();

    private String importsComponentList;


    public ScriptEntity createScriptEntity(ScriptRepo scriptRepo) {
        ScriptEntity scriptEntity = new ScriptEntity();
        scriptEntity.setName(name);
        scriptEntity.setDescription(description);
        scriptEntity.setContent(content);
        if(!Objects.isNull(importsComponentList))
            scriptEntity.setImportsComponentList(importsComponentList);
        scriptEntity = scriptRepo.save(scriptEntity);
        return scriptEntity;
    }

    public List<ScriptMethodEntity> createScriptMethodEntityList(ScriptMethodRepo scriptMethodRepo) {
        List<ScriptMethodEntity> methodEntityList = new ArrayList<>();
        if(!Objects.isNull(methodList)) {
            for (ScriptMethod method : methodList) {
                ScriptMethodEntity methodEntity = method.createScriptMethodEntity(scriptMethodRepo);
                methodEntityList.add(methodEntity);
            }
        }
        return methodEntityList;
    }

    public List<ScriptDataEntity> createScriptDataEntityList(ScriptDataRepo scriptDataRepo) {
        List<ScriptDataEntity> dataEntityList = new ArrayList<>();
        if(!Objects.isNull(dataList)) {
            for (ScriptData data : dataList) {
                ScriptDataEntity dataEntity = data.createScriptDataEntity(scriptDataRepo);
                dataEntityList.add(dataEntity);
            }
        }
        return dataEntityList;
    }

}