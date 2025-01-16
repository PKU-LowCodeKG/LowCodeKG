package org.example.lowcodekg.schema.entity.workflow;

import com.alibaba.fastjson.JSONObject;
import io.micrometer.common.util.StringUtils;
import lombok.*;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JavaMethod {

    private Long vid;

    private String name;

    private String fullName;

    private String projectName;

    private String returnType;

    private String fullReturnType;

    private String visibility;

    /**
     * 方法修饰符，如static、abstract、final、synchronized、native等
     */
    private String modifier;

    /**
     * 请求路径（Controller类）
     */
    private String mappingUrl;

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
    private List<JavaClass> paramTypeList = new ArrayList<>();
    private List<JavaClass> returnTypeList = new ArrayList<>();
    private List<JavaClass> variableTypeList = new ArrayList<>();
    private List<JavaMethod> methodCallList = new ArrayList<>();
    private List<JavaField> fieldAccessList = new ArrayList<>();


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

    public JavaMethodEntity storeInNeo4j(JavaMethodRepo javaMethodRepo, JSONObject jsonContent) {
        JavaMethodEntity methodEntity = new JavaMethodEntity();
        methodEntity.setName(name);
        methodEntity.setFullName(fullName);
        methodEntity.setProjectName(projectName);
        methodEntity.setReturnType(returnType);
        methodEntity.setContent(content);
        methodEntity.setComment(comment);
        methodEntity.setParams(params);
        if(!Objects.isNull(jsonContent)) {
            methodEntity.setVid(jsonContent.getLong("id"));
            methodEntity.setDescription(jsonContent.getString("description"));
        }
        return javaMethodRepo.save(methodEntity);
    }

    /**
     * 判断方法是否属于工作流
     */
    public boolean belongToWorkflow() {
        return (StringUtils.isNotEmpty(mappingUrl) && !"".equals(mappingUrl));
    }

}