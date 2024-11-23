package org.example.lowcodekg.schema.entity.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.example.lowcodekg.dao.neo4j.entity.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class JavaMethod {

    private String name;

    private String fullName;

    private String returnType;

    private String fullReturnType;

    private String visibility;

    /**
     * 方法修饰符，如static、abstract、final、synchronized、native等
     */
    private String modifier;

    private String content;

    private String comment;

    private String description;

    private String params;

    private String fullParams;

    private IMethodBinding methodBiding;

    private String belongTo;

    private String fieldAccesses;

    private String fullVariables;

    private Set<IMethodBinding> methodCalls;

    private String throwType;

    /**
     * 记录实体间关系
     */
    private List<JavaClass> paramTypeList;
    private List<JavaClass> returnTypeList;
    private List<JavaClass> variableTypeList;
    private List<JavaMethod> methodCallList;
    private List<JavaField> fieldAccessList;


    public JavaMethod(String name, String fullName, String returnType, String content, String comment, String params, IMethodBinding methodBinding,
                          String fullReturnType, String belongTo, String fullParams, String fullVariables, Set<IMethodBinding> methodCalls, String fieldAccesses, String throwTypes) {
        this.name = name;
        this.fullName = fullName;
        this.returnType = returnType;
        this.content = content;
        this.comment = comment;
        this.params = params;
        this.methodBiding = methodBinding;
        this.fullReturnType = fullReturnType;
        this.belongTo = belongTo;
        this.fullParams = fullParams;
        this.methodCalls = methodCalls;
        this.fullVariables = fullVariables;
        this.fieldAccesses = fieldAccesses;
        this.throwType = throwTypes;
    }

    public JavaMethodEntity storeInNeo4j(JavaMethodRepo javaMethodRepo) {
        JavaMethodEntity methodEntity = new JavaMethodEntity();
        methodEntity.setName(name);
        methodEntity.setFullName(fullName);
        methodEntity.setReturnType(returnType);
        methodEntity.setContent(content);
        methodEntity.setComment(comment);
        methodEntity.setDescription(description);
        methodEntity.setParams(params);
        javaMethodRepo.save(methodEntity);
        return methodEntity;
    }


}