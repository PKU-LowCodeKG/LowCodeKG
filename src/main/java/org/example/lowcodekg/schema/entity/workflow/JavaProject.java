package org.example.lowcodekg.schema.entity.workflow;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaFieldRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JavaProject {
    @Getter
    @Setter
    private String projectName;

    private Map<String, JavaClass> classMap = new HashMap<>();

    private Map<String, JavaMethod> methodMap = new HashMap<>();

    private Map<String, JavaField> fieldMap = new HashMap<>();

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

    public void storeInNeo4j(JavaClassRepo javaClassRepo, JavaMethodRepo javaMethodRepo, JavaFieldRepo javaFieldRepo) {

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
