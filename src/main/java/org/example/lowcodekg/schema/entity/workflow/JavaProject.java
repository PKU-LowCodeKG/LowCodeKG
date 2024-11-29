package org.example.lowcodekg.schema.entity.workflow;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.example.lowcodekg.dao.neo4j.entity.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.JavaFieldEntity;
import org.example.lowcodekg.dao.neo4j.entity.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaFieldRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;
import org.example.lowcodekg.service.ElasticSearchService;
import org.example.lowcodekg.util.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JavaProject {

    @Value("${json.path}")
    private final String jsonFilePath = "/Users/xianlin/Desktop/workspaces/software_reuse/LowCodeKG/src/main/resources/data/javaInfo.json";

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
    public void parseRelations(JavaClassRepo javaClassRepo, JavaMethodRepo javaMethodRepo, JavaFieldRepo javaFieldRepo) {
        // load local json file
        Map<String, JSONObject> jsonMap = JSONUtils.loadJsonFile(jsonFilePath);
        System.out.println("jsonObject number:" + jsonMap.size());

        methodMap.values().forEach(info -> methodBindingMap.put(info.getMethodBiding(), info));

        /*
         * create JavaClassEntity
         */
        classMap.values().forEach(classInfo -> {
            classInfo.setProjectName(projectName);
            classInfo.getSuperClassList().addAll(findJavaClassInfo(classInfo.getSuperClassType()));
            classInfo.getSuperInterfaceList().addAll(findJavaClassInfo(classInfo.getSuperInterfaceType()));
            JavaClassEntity classEntity = classInfo.storeInNeo4j(javaClassRepo, jsonMap.get(classInfo.getFullName()));
            classEntityMap.put(classInfo.getFullName(), classEntity);

            // vector store
            classInfo.setVid(classEntity.getVid());
            classInfo.setDescription(classEntity.getDescription());
            elasticSearchService.storeJavaClassEmbedding(classInfo);
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

        /*
         * create JavaMethodEntity
         */
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

           // vector store
           methodInfo.setVid(methodEntity.getVid());
           methodInfo.setDescription(methodEntity.getDescription());
           elasticSearchService.storeJavaMethodEmbedding(methodInfo);
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

        /*
         * create JavaFieldEntity
         */
        fieldMap.values().forEach(fieldInfo -> {
            fieldInfo.setProjectName(projectName);
            findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> owner.getContainFieldList().add(fieldInfo));
            findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> fieldInfo.getFiledTypeList().add(type));

            JavaFieldEntity fieldEntity = fieldInfo.storeInNeo4j(javaFieldRepo, jsonMap.get(fieldInfo.getFullName()));
            fieldEntityMap.put(fieldInfo.getFullName(), fieldEntity);

            // vector store
            fieldInfo.setVid(fieldEntity.getVid());
            fieldInfo.setDescription(fieldEntity.getDescription());
            elasticSearchService.storeJavaFieldEmbedding(fieldInfo);
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

    public void storeRelations(JavaClassRepo javaClassRepo, JavaMethodRepo javaMethodRepo, JavaFieldRepo javaFieldRepo) {
        // class outgoing relations
        classEntityMap.values().forEach(classEntity -> {
            javaClassRepo.saveAll(classEntity.getSuperClassList());
            javaClassRepo.saveAll(classEntity.getSuperInterfaceList());
            javaMethodRepo.saveAll(classEntity.getMethodList());
            javaFieldRepo.saveAll(classEntity.getFieldList());
        });
        // method outgoing relations
        methodEntityMap.values().forEach(methodEntity -> {
            javaMethodRepo.saveAll(methodEntity.getMethodCallList());
            javaFieldRepo.saveAll(methodEntity.getFieldAccessList());
            javaClassRepo.saveAll(methodEntity.getParamTypeList());
            javaClassRepo.saveAll(methodEntity.getReturnTypeList());
            javaClassRepo.saveAll(methodEntity.getVariableTypeList());
        });
        // field outgoing relations
        fieldEntityMap.values().forEach(fieldEntity -> {
            javaClassRepo.saveAll(fieldEntity.getTypeList());
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
