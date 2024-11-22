package org.example.lowcodekg.schema.entity.workflow;

import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.HashMap;
import java.util.Map;

public class JavaProject {
    
    private Map<String, JavaClass> classInfoMap = new HashMap<>();
    private Map<String, JavaMethod> methodInfoMap = new HashMap<>();
    private Map<String, JavaField> fieldInfoMap = new HashMap<>();

    private Map<IMethodBinding, JavaMethod> methodBindingMap = new HashMap<>();

    public void addClassInfo(JavaClass info) {
        classInfoMap.put(info.getFullName(), info);
    }

    public void addMethodInfo(JavaMethod info) {
        methodInfoMap.put(info.getFullName(), info);
        methodBindingMap.put(info.getMethodBinding(), info);
    }

    public void addFieldInfo(JavaField info) {
        fieldInfoMap.put(info.getFullName(), info);
    }

    public void parseRels(BatchInserter inserter) {
        methodInfoMap.values().forEach(info -> methodBindingMap.put(info.getMethodBinding(), info));
        classInfoMap.values().forEach(classInfo -> {
            findJavaClass(classInfo.getSuperClassType()).forEach(superClassInfo -> inserter.createRelationship(classInfo.getNodeId(), superClassInfo.getNodeId(), JavaExtractor.EXTEND, new HashMap<>()));
            findJavaClass(classInfo.getSuperInterfaceTypes()).forEach(superInterfaceInfo -> inserter.createRelationship(classInfo.getNodeId(), superInterfaceInfo.getNodeId(), JavaExtractor.IMPLEMENT, new HashMap<>()));
        });
        methodInfoMap.values().forEach(methodInfo -> {
            findJavaClass(methodInfo.getBelongTo()).forEach(owner -> inserter.createRelationship(owner.getNodeId(), methodInfo.getNodeId(), JavaExtractor.HAVE_METHOD, new HashMap<>()));
            findJavaClass(methodInfo.getFullParams()).forEach(param -> inserter.createRelationship(methodInfo.getNodeId(), param.getNodeId(), JavaExtractor.PARAM_TYPE, new HashMap<>()));
            findJavaClass(methodInfo.getFullReturnType()).forEach(rt -> inserter.createRelationship(methodInfo.getNodeId(), rt.getNodeId(), JavaExtractor.RETURN_TYPE, new HashMap<>()));
            findJavaClass(methodInfo.getThrowTypes()).forEach(tr -> inserter.createRelationship(methodInfo.getNodeId(), tr.getNodeId(), JavaExtractor.THROW_TYPE, new HashMap<>()));
            findJavaClass(methodInfo.getFullVariables()).forEach(var -> inserter.createRelationship(methodInfo.getNodeId(), var.getNodeId(), JavaExtractor.VARIABLE_TYPE, new HashMap<>()));
            methodInfo.getMethodCalls().forEach(call -> {
                if (methodBindingMap.containsKey(call))
                    inserter.createRelationship(methodInfo.getNodeId(), methodBindingMap.get(call).getNodeId(), JavaExtractor.METHOD_CALL, new HashMap<>());
            });
            findJavaField(methodInfo.getFieldAccesses()).forEach(access -> inserter.createRelationship(methodInfo.getNodeId(), access.getNodeId(), JavaExtractor.FIELD_ACCESS, new HashMap<>()));
        });
        fieldInfoMap.values().forEach(fieldInfo -> {
            findJavaClass(fieldInfo.getBelongTo()).forEach(owner -> inserter.createRelationship(owner.getNodeId(), fieldInfo.getNodeId(), JavaExtractor.HAVE_FIELD, new HashMap<>()));
            findJavaClass(fieldInfo.getFullType()).forEach(type -> inserter.createRelationship(fieldInfo.getNodeId(), type.getNodeId(), JavaExtractor.FIELD_TYPE, new HashMap<>()));
        });
    }

    private Set<JavaClass> findJavaClass(String str) {
        Set<JavaClass> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (classInfoMap.containsKey(token))
                r.add(classInfoMap.get(token));
        return r;
    }

    private Set<JavaField> findJavaField(String str) {
        Set<JavaField> r = new HashSet<>();
        String[] tokens = str.split("[^\\w\\.]+");
        for (String token : tokens)
            if (fieldInfoMap.containsKey(token))
                r.add(fieldInfoMap.get(token));
        return r;
    }
}
