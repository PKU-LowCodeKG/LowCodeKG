package org.example.lowcodekg.model.schema.entity.workflow;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaFieldEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaFieldRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaMethodRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.common.util.JsonUtil;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.util.summarize.FuncGenerate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class JavaProject {

    private final String jsonFilePath = "/src/main/resources/data/javaInfo_1210.json";
    private final FuncGenerate funcGenerate;
    private Map<String, JSONObject> jsonMap = new HashMap<>();

    @Setter
    private ElasticSearchService elasticSearchService;

    @Getter
    @Setter
    private String projectName;

    private Map<String, JavaClass> classMap = new HashMap<>();
    private Map<String, JavaClassEntity> classEntityMap = new HashMap<>();

    private Map<String, JavaMethod> methodMap = new HashMap<>();
    private Map<String, JavaMethodEntity> methodEntityMap = new HashMap<>();

    private Map<String, JavaField> fieldMap = new HashMap<>();
    private Map<String, JavaFieldEntity> fieldEntityMap = new HashMap<>();

    private Map<IMethodBinding, JavaMethod> methodBindingMap = new HashMap<>();

    public JavaProject(FuncGenerate funcGenerate) {
        this.funcGenerate = funcGenerate;
    }


    public void init() {
        // load local json file for description
        String projectDir = System.getProperty("user.dir");
        File file = new File(projectDir + "/" + jsonFilePath);
        if(!file.exists()) {
            System.out.println("json file not found");
            return;
        }
        jsonMap = JsonUtil.loadJsonFile(file.getAbsolutePath());
        System.out.println("jsonObject number:" + jsonMap.size());
    }

    public void addClass(JavaClass javaClass) {
        classMap.put(javaClass.getFullName(), javaClass);
    }

    public void addMethod(JavaMethod javaMethod) {
        methodMap.put(javaMethod.getFullName(), javaMethod);
        methodBindingMap.put(javaMethod.getMethodBiding(), javaMethod);
    }

    public void addField(JavaField javaField) {
        fieldMap.put(javaField.getFullName(), javaField);
    }

    /**
     * 创建实体及关系
     */
    public void parse(WorkflowRepo workflowRepo,
                      JavaClassRepo javaClassRepo,
                      JavaMethodRepo javaMethodRepo,
                      JavaFieldRepo javaFieldRepo) {
        methodMap.values().forEach(info -> methodBindingMap.put(info.getMethodBiding(), info));

        /*
         * create entities
         */
        parseClassEntity(javaClassRepo);
        parseMethodEntity(javaMethodRepo, workflowRepo);
        parseFieldEntity(javaFieldRepo);

        /*
         * store relationships
         */
        parseRelations(javaClassRepo, javaMethodRepo, javaFieldRepo);
    }

    private void parseClassEntity(JavaClassRepo javaClassRepo) {
        classMap.values().forEach(classInfo -> {
            classInfo.setProjectName(projectName);
            classInfo.getSuperClassList().addAll(findJavaClassInfo(classInfo.getSuperClassType()));
            classInfo.getSuperInterfaceList().addAll(findJavaClassInfo(classInfo.getSuperInterfaceType()));
            JavaClassEntity classEntity = classInfo.storeInNeo4j(javaClassRepo, jsonMap.get(classInfo.getFullName()));
            // 判定为数据实体类，生成描述信息并添加索引
            if(classInfo.getIsData()) {
                funcGenerate.genDataObjectFunc(classEntity);
            }
            classEntityMap.put(classInfo.getFullName(), classEntity);
        });
        // class -[extend | implement]-> class
        classMap.values().forEach(classInfo -> {
            JavaClassEntity classEntity = classEntityMap.get(classInfo.getFullName());
            if(!Objects.isNull(classEntity)) {
                classEntity.getSuperClassList().addAll(
                        classInfo.getSuperClassList().stream().map(cls ->
                                classEntityMap.get(cls.getFullName())).toList());
                classEntity.getSuperInterfaceList().addAll(
                        classInfo.getSuperInterfaceList().stream().map(cls ->
                                classEntityMap.get(cls.getFullName())).toList());
            }
        });
    }

    private void parseMethodEntity(JavaMethodRepo javaMethodRepo, WorkflowRepo workflowRepo) {
        methodMap.values().forEach(methodInfo -> {
            methodInfo.setProjectName(projectName);
            findJavaClassInfo(methodInfo.getBelongTo()).forEach(owner -> owner.getContainMethodList().add(methodInfo));
            findJavaClassInfo(methodInfo.getFullParams()).forEach(param -> methodInfo.getParamTypeList().add(param));
            findJavaClassInfo(methodInfo.getFullReturnType()).forEach(returnType -> methodInfo.getReturnTypeList().add(returnType));
            findJavaClassInfo(methodInfo.getFullVariables()).forEach(variable -> methodInfo.getVariableTypeList().add(variable));
            methodInfo.getMethodCalls().forEach(call -> {
                if (methodBindingMap.containsKey(call)) {
                    methodInfo.getMethodCallList().add(methodBindingMap.get(call));
                }
            });
            findJavaFieldInfo(methodInfo.getFieldAccesses()).forEach(access -> methodInfo.getFieldAccessList().add(access));

            JavaMethodEntity methodEntity = methodInfo.storeInNeo4j(javaMethodRepo, jsonMap.get(methodInfo.getFullName()));
            methodEntityMap.put(methodInfo.getFullName(), methodEntity);

            // check if the method belongs to workflow
            if(methodInfo.belongToWorkflow()) {
                Workflow workflow = new Workflow(methodInfo);
                WorkflowEntity workflowEntity = workflow.createWorkflowEntity(workflowRepo);
                workflowRepo.createRelationOfContainedMethod(workflowEntity.getId(), methodEntity.getId());
            }
        });
        // class -[have_method]-> method
        classMap.values().forEach(classInfo -> {
            JavaClassEntity classEntity = classEntityMap.get(classInfo.getFullName());
            if(!Objects.isNull(classEntity)) {
                classEntity.getMethodList().addAll(
                        classInfo.getContainMethodList().stream().map(method ->
                                methodEntityMap.get(method.getFullName())).toList());
            }
        });
        // method -[param_type | return_type | variable_type]-> class
        // method -[method_call]-> method
        methodMap.values().forEach(methodInfo -> {
            JavaMethodEntity methodEntity = methodEntityMap.get(methodInfo.getFullName());
            if(!Objects.isNull(methodEntity)) {
                methodEntity.getParamTypeList().addAll(
                        methodInfo.getParamTypeList().stream().map(param ->
                                classEntityMap.get(param.getFullName())).toList());
                methodEntity.getReturnTypeList().addAll(
                        methodInfo.getReturnTypeList().stream().map(returnType ->
                                classEntityMap.get(returnType.getFullName())).toList());
                methodEntity.getVariableTypeList().addAll(
                        methodInfo.getVariableTypeList().stream().map(variable ->
                                classEntityMap.get(variable.getFullName())).toList());
                methodEntity.getMethodCallList().addAll(
                        methodInfo.getMethodCallList().stream().map(call ->
                                methodEntityMap.get(call.getFullName())).toList());
            }
        });
    }

    private void parseFieldEntity(JavaFieldRepo javaFieldRepo) {
        fieldMap.values().forEach(fieldInfo -> {
            fieldInfo.setProjectName(projectName);
            findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> owner.getContainFieldList().add(fieldInfo));
            findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> fieldInfo.getFiledTypeList().add(type));

            JavaFieldEntity fieldEntity = fieldInfo.storeInNeo4j(javaFieldRepo, jsonMap.get(fieldInfo.getFullName()));
            fieldEntityMap.put(fieldInfo.getFullName(), fieldEntity);
        });
        // class -[have_field]-> field
        classMap.values().forEach(classInfo -> {
            JavaClassEntity classEntity = classEntityMap.get(classInfo.getFullName());
            if(!Objects.isNull(classEntity)) {
                classEntity.getFieldList().addAll(
                        classInfo.getContainFieldList().stream().map(field ->
                                fieldEntityMap.get(field.getFullName())).toList());
            }
        });
        // method -[field_access]-> field
        methodMap.values().forEach(methodInfo -> {
            JavaMethodEntity methodEntity = methodEntityMap.get(methodInfo.getFullName());
            if(!Objects.isNull(methodEntity)) {
                methodEntity.getFieldAccessList().addAll(
                        methodInfo.getFieldAccessList().stream().map(access ->
                                fieldEntityMap.get(access.getFullName())).toList());
            }
        });
        // field -[filed_type]-> class
        fieldMap.values().forEach(fieldInfo -> {
            JavaFieldEntity fieldEntity = fieldEntityMap.get(fieldInfo.getFullName());
            if(!Objects.isNull(fieldEntity)) {
                fieldEntity.getTypeList().addAll(
                        fieldInfo.getFiledTypeList().stream().map(type ->
                                classEntityMap.get(type.getFullName())).toList());
            }
        });
    }

    private void parseRelations(JavaClassRepo javaClassRepo, JavaMethodRepo javaMethodRepo, JavaFieldRepo javaFieldRepo) {
        classEntityMap.values().forEach(classEntity -> {
            classEntity.getSuperClassList().forEach(superClass -> {
                javaClassRepo.createRelationOfExtendClass(classEntity.getId(), superClass.getId());
            });
            classEntity.getSuperInterfaceList().forEach(superInterface -> {
                javaClassRepo.createRelationOfInterface(classEntity.getId(), superInterface.getId());
            });
            classEntity.getMethodList().forEach(methodEntity -> {
                javaClassRepo.createRelationOfMethod(classEntity.getId(), methodEntity.getId());
            });
            classEntity.getFieldList().forEach(fieldEntity -> {
                javaClassRepo.createRelationOfField(classEntity.getId(), fieldEntity.getId());
            });

        });
        methodEntityMap.values().forEach(methodEntity -> {
            methodEntity.getMethodCallList().forEach(call -> {
                javaMethodRepo.createRelationOfMethodCall(methodEntity.getId(), call.getId());
            });
            methodEntity.getParamTypeList().forEach(param -> {
                javaMethodRepo.createRelationOfParamType(methodEntity.getId(), param.getId());
            });
            methodEntity.getReturnTypeList().forEach(returnType -> {
                javaMethodRepo.createRelationOfReturnType(methodEntity.getId(), returnType.getId());
            });
            methodEntity.getVariableTypeList().forEach(variable -> {
                javaMethodRepo.createRelationOfVariableType(methodEntity.getId(), variable.getId());
            });
            methodEntity.getFieldAccessList().forEach(access -> {
                javaMethodRepo.createRelationOfFieldAccess(methodEntity.getId(), access.getId());
            });
        });
        fieldEntityMap.values().forEach(fieldEntity -> {
            fieldEntity.getTypeList().forEach(type -> {
                javaFieldRepo.createRelationOfFieldType(fieldEntity.getId(), type.getId());
            });
        });
    }

    private Set<JavaClass> findJavaClassInfo(String str) {
        Set<JavaClass> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (classMap.containsKey(token))
                r.add(classMap.get(token));
        return r;
    }

    private Set<JavaField> findJavaFieldInfo(String str) {
        Set<JavaField> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (fieldMap.containsKey(token))
                r.add(fieldMap.get(token));
        return r;
    }
}
