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
import org.example.lowcodekg.util.JSONUtils;

import java.util.*;

public class JavaProject {
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
        Map<String, JSONObject> jsonMap = JSONUtils.loadJsonFile("/Users/chang/Documents/projects/LowCodeKG/src/main/resources/data/javaInfo.json");

        methodMap.values().forEach(info -> methodBindingMap.put(info.getMethodBiding(), info));

        // record created JavaClassEntity
        classMap.values().forEach(classInfo -> {
            classInfo.getSuperClassList().addAll(findJavaClassInfo(classInfo.getSuperClassType()));
            classInfo.getSuperInterfaceList().addAll(findJavaClassInfo(classInfo.getSuperInterfaceType()));
            classEntityMap.put(classInfo.getFullName(), classInfo.storeInNeo4j(javaClassRepo, jsonMap.get(classInfo.getFullName())));
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

        methodMap.values().forEach(methodInfo -> {
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
           methodEntityMap.put(methodInfo.getFullName(), methodInfo.storeInNeo4j(javaMethodRepo, jsonMap.get(methodInfo.getFullName())));
        });
        // class -[have_method]-> method
        classMap.values().forEach(classInfo -> {
            JavaClassEntity classEntity = classEntityMap.get(classInfo.getFullName());
            if(!Objects.isNull(classEntity)) {
                classEntity.getMethodList().addAll(
                        classInfo.getContainMethodList().stream().map(method ->
                                methodEntityMap.get(method.getFullName())).toList());
                // debug
//                System.out.println("class " + classInfo.getName() + " have_methods " + classInfo.getContainMethodList());
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

        fieldMap.values().forEach(fieldInfo -> {
            findJavaClassInfo(fieldInfo.getBelongTo()).forEach(owner -> owner.getContainFieldList().add(fieldInfo));
            findJavaClassInfo(fieldInfo.getFullType()).forEach(type -> fieldInfo.getFiledTypeList().add(type));
            fieldEntityMap.put(fieldInfo.getFullName(), fieldInfo.storeInNeo4j(javaFieldRepo, jsonMap.get(fieldInfo.getFullName())));
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
