package org.example.lowcodekg.extraction.workflow;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import org.eclipse.jdt.core.dom.*;
import org.example.lowcodekg.model.schema.entity.workflow.JavaClass;
import org.example.lowcodekg.model.schema.entity.workflow.JavaField;
import org.example.lowcodekg.model.schema.entity.workflow.JavaMethod;
import org.example.lowcodekg.model.schema.entity.workflow.JavaProject;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JavaASTVisitor extends ASTVisitor {

    private JavaProject javaProject;

    private String sourceContent;

    @Override
    public boolean visit(TypeDeclaration node) {
        JavaClass javaClassInfo = createJavaClassInfo(node);
        javaProject.addClass(javaClassInfo);

        MethodDeclaration[] methodDeclarations = node.getMethods();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            JavaMethod javaMethodInfo = createJavaMethodInfo(methodDeclaration, javaClassInfo.getFullName());
            if (javaMethodInfo != null) {
                javaProject.addMethod(javaMethodInfo);
            }
        }

        FieldDeclaration[] fieldDeclarations = node.getFields();
        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            List<JavaField> javaFieldInfos = createJavaFieldInfos(fieldDeclaration, javaClassInfo.getFullName());
            for (JavaField javaFieldInfo : javaFieldInfos) {
                javaProject.addField(javaFieldInfo);
            }
        }

        return false;
    }

    private JavaClass createJavaClassInfo(TypeDeclaration node) {
        String name = node.getName().getFullyQualifiedName();
        String fullName = NameResolver.getFullName(node);
        boolean isInterface = node.isInterface();
        boolean isAbstract = Modifier.isAbstract(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        String content = sourceContent.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
        String superClassType = node.getSuperclassType() == null ? "java.lang.Object" : NameResolver.getFullName(node.getSuperclassType());
        String superInterfaceTypes = String.join(", ", (List<String>) node.superInterfaceTypes().stream().map(n -> NameResolver.getFullName((Type) n)).collect(Collectors.toList()));
        JavaClass classInfo = new JavaClass(name, fullName, comment, content, superClassType, superInterfaceTypes);
        // 是否是数据实体类
        // 两层判断逻辑：1.判断是否是@Data注解修饰的类；2.判断路径名是否包含entity
        List<IExtendedModifier> annotations = node.modifiers();
        if(fullName.contains("entity") || fullName.contains("model") || fullName.contains("domain")) {
            classInfo.setIsData(true);
        } else {
            for(IExtendedModifier modifier : annotations) {
                if(modifier instanceof Annotation) {
                    Annotation annotation = (Annotation) modifier;
                    String annotationName = annotation.getTypeName().toString();
                    if (annotationName.equals("Data") || annotationName.contains("TableName")) {
                        classInfo.setIsData(true);
                    }
                }
            }
        }
        return classInfo;

    }

    private JavaMethod createJavaMethodInfo(MethodDeclaration node, String belongTo) {
        IMethodBinding methodBinding = node.resolveBinding();
        if (methodBinding == null)
            return null;
        String name = node.getName().getFullyQualifiedName();
        Type type = node.getReturnType2();
        String returnType = type == null ? "void" : type.toString();
        String fullReturnType = NameResolver.getFullName(type);
        boolean isConstruct = node.isConstructor();
        boolean isAbstract = Modifier.isAbstract(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        boolean isStatic = Modifier.isStatic(node.getModifiers());
        boolean isSynchronized = Modifier.isSynchronized(node.getModifiers());
        String content = sourceContent.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        String params = String.join(", ", (List<String>) node.parameters().stream().map(n -> {
            SingleVariableDeclaration param = (SingleVariableDeclaration) n;
            return (Modifier.isFinal(param.getModifiers()) ? "final " : "") + param.getType().toString() + " " + param.getName().getFullyQualifiedName();
        }).collect(Collectors.toList()));
        String fullName = belongTo + "." + name;
        String fullParams = String.join(", ", (List<String>) node.parameters().stream().map(n -> {
            SingleVariableDeclaration param = (SingleVariableDeclaration) n;
            return NameResolver.getFullName(param.getType());
        }).collect(Collectors.toList()));
        String throwTypes = String.join(", ", (List<String>) node.thrownExceptionTypes().stream().map(n -> NameResolver.getFullName((Type) n)).collect(Collectors.toList()));
        Set<IMethodBinding> methodCalls = new HashSet<>();
        StringBuilder fullVariables = new StringBuilder();
        StringBuilder fieldAccesses = new StringBuilder();
        parseMethodBody(methodCalls, fullVariables, fieldAccesses, node.getBody());
        JavaMethod info = new JavaMethod(name, fullName, returnType, content, comment, params, methodBinding,
                fullReturnType, belongTo, fullParams, fullVariables.toString(), methodCalls, fieldAccesses.toString(), throwTypes);
        // check annotation
        List<IExtendedModifier> annotations = node.modifiers();
        for(IExtendedModifier modifier : annotations) {
            if(modifier instanceof Annotation annotation) {
                String annotationName = annotation.getTypeName().toString();
                if (annotationName.equals("GetMapping") || annotationName.equals("PostMapping")
                        || annotationName.equals("PutMapping") || annotationName.equals("DeleteMapping")
                        || annotationName.equals("RequestMapping")) {
                    // 提取路由路径
                    String mappingUrl = null;
                    if (annotation instanceof NormalAnnotation normalAnnotation) {
                        for (Object obj : normalAnnotation.values()) { // 修改这里
                            if (obj instanceof MemberValuePair) { // 添加类型检查
                                MemberValuePair pair = (MemberValuePair) obj;
                                if (pair.getName().toString().equals("value")) {
                                    Expression expression = pair.getValue();
                                    if (expression instanceof StringLiteral literal) {
                                        mappingUrl = "@" + annotationName + "(\"" + literal.getLiteralValue() + "\")";
                                    }
                                }
                            }
                        }
                    } else if (annotation instanceof SingleMemberAnnotation singleMemberAnnotation) {
                        Expression expression = singleMemberAnnotation.getValue();
                        if (expression instanceof StringLiteral) {
                            StringLiteral literal = (StringLiteral) expression;
                            mappingUrl = "@" + annotationName + "(\"" + literal.getLiteralValue() + "\")";
                        }
                    }
                    if(StringUtils.isNotEmpty(mappingUrl)) {
//                        mappingUrl = mappingUrl.substring(1).replaceAll("/", "_");
                        info.setMappingUrl(mappingUrl);
                    }
                }
            }
        }
        return info;
    }

    private List<JavaField> createJavaFieldInfos(FieldDeclaration node, String belongTo) {
        List<JavaField> r = new ArrayList<>();
        String type = node.getType().toString();
        String fullType = NameResolver.getFullName(node.getType());
        boolean isStatic = Modifier.isStatic(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        node.fragments().forEach(n -> {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) n;
            String name = fragment.getName().getFullyQualifiedName();
            String fullName = belongTo + "." + name;
            r.add(new JavaField(name, fullName, type, comment, belongTo, fullType));
        });
        return r;
    }

    private void parseMethodBody(Set<IMethodBinding> methodCalls, StringBuilder fullVariables, StringBuilder fieldAccesses, Block methodBody) {
        if (methodBody == null)
            return;
        List<Statement> statementList = methodBody.statements();
        List<Statement> statements = new ArrayList<>();
        for (int i = 0; i < statementList.size(); i++) {
            statements.add(statementList.get(i));
        }
        for (int i = 0; i < statements.size(); i++) {

            if (statements.get(i).getNodeType() == ASTNode.BLOCK) {
                List<Statement> blockStatements = ((Block) statements.get(i)).statements();
                for (int j = 0; j < blockStatements.size(); j++) {
                    statements.add(i + j + 1, blockStatements.get(j));
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.ASSERT_STATEMENT) {
                Expression expression = ((AssertStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                expression = ((AssertStatement) statements.get(i)).getMessage();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.DO_STATEMENT) {
                Expression expression = ((DoStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement doBody = ((DoStatement) statements.get(i)).getBody();
                if (doBody != null) {
                    statements.add(i + 1, doBody);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT) {
                Expression expression = ((EnhancedForStatement) statements.get(i)).getExpression();
                Type type = ((EnhancedForStatement) statements.get(i)).getParameter().getType();
                fullVariables.append(NameResolver.getFullName(type) + ", ");
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement forBody = ((EnhancedForStatement) statements.get(i)).getBody();
                if (forBody != null) {
                    statements.add(i + 1, forBody);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
                Expression expression = ((ExpressionStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.FOR_STATEMENT) {
                List<Expression> list = ((ForStatement) statements.get(i)).initializers();
                for (int j = 0; j < list.size(); j++) {
                    parseExpression(methodCalls, fieldAccesses, list.get(j));
                }
                Expression expression = ((ForStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement forBody = ((ForStatement) statements.get(i)).getBody();
                if (forBody != null) {
                    statements.add(i + 1, forBody);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.IF_STATEMENT) {
                Expression expression = ((IfStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement thenStatement = ((IfStatement) statements.get(i)).getThenStatement();
                Statement elseStatement = ((IfStatement) statements.get(i)).getElseStatement();
                if (elseStatement != null) {
                    statements.add(i + 1, elseStatement);
                }
                if (thenStatement != null) {
                    statements.add(i + 1, thenStatement);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.RETURN_STATEMENT) {
                Expression expression = ((ReturnStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.SWITCH_STATEMENT) {
                Expression expression = ((SwitchStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                List<Statement> switchStatements = ((SwitchStatement) statements.get(i)).statements();
                for (int j = 0; j < switchStatements.size(); j++) {
                    statements.add(i + j + 1, switchStatements.get(j));
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.THROW_STATEMENT) {
                Expression expression = ((ThrowStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.TRY_STATEMENT) {
                Statement tryStatement = ((TryStatement) statements.get(i)).getBody();
                if (tryStatement != null) {
                    statements.add(i + 1, tryStatement);
                }
                continue;
            }
            else if (statements.get(i).getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
                Type type = ((VariableDeclarationStatement) statements.get(i)).getType();
                fullVariables.append(NameResolver.getFullName(type) + ", ");
                ((VariableDeclarationStatement) statements.get(i)).fragments().forEach(n -> parseExpression(methodCalls, fieldAccesses, ((VariableDeclaration) n).getInitializer()));
            }
            else if (statements.get(i).getNodeType() == ASTNode.WHILE_STATEMENT) {
                Expression expression = ((WhileStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(methodCalls, fieldAccesses, expression);
                }
                Statement whileBody = ((WhileStatement) statements.get(i)).getBody();
                if (whileBody != null) {
                    statements.add(i + 1, whileBody);
                }
            }
        }
    }

    private void parseExpression(Set<IMethodBinding> methodCalls, StringBuilder fieldAccesses, Expression expression) {
        if (expression == null) {
            return;
        }
        else if (expression.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
            List<Expression> expressions = ((ArrayInitializer) expression).expressions();
            for (Expression expression2 : expressions) {
                parseExpression(methodCalls, fieldAccesses, expression2);
            }
        }
        else if (expression.getNodeType() == ASTNode.CAST_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((CastExpression) expression).getExpression());
        }
        else if (expression.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((ConditionalExpression) expression).getExpression());
            parseExpression(methodCalls, fieldAccesses, ((ConditionalExpression) expression).getElseExpression());
            parseExpression(methodCalls, fieldAccesses, ((ConditionalExpression) expression).getThenExpression());
        }
        else if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((InfixExpression) expression).getLeftOperand());
            parseExpression(methodCalls, fieldAccesses, ((InfixExpression) expression).getRightOperand());
        }
        else if (expression.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((InstanceofExpression) expression).getLeftOperand());
        }
        else if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((ParenthesizedExpression) expression).getExpression());
        }
        else if (expression.getNodeType() == ASTNode.POSTFIX_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((PostfixExpression) expression).getOperand());
        }
        else if (expression.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((PrefixExpression) expression).getOperand());
        }
        else if (expression.getNodeType() == ASTNode.THIS_EXPRESSION) {
            parseExpression(methodCalls, fieldAccesses, ((ThisExpression) expression).getQualifier());
        }
        else if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
            List<Expression> arguments = ((MethodInvocation) expression).arguments();
            IMethodBinding methodBinding = ((MethodInvocation) expression).resolveMethodBinding();
            if (methodBinding != null)
                methodCalls.add(methodBinding);
            for (Expression exp : arguments)
                parseExpression(methodCalls, fieldAccesses, exp);
            parseExpression(methodCalls, fieldAccesses, ((MethodInvocation) expression).getExpression());
        }
        else if (expression.getNodeType() == ASTNode.ASSIGNMENT) {
            parseExpression(methodCalls, fieldAccesses, ((Assignment) expression).getLeftHandSide());
            parseExpression(methodCalls, fieldAccesses, ((Assignment) expression).getRightHandSide());
        }
        else if (expression.getNodeType() == ASTNode.QUALIFIED_NAME) {
            if (((QualifiedName) expression).getQualifier().resolveTypeBinding() != null) {
                String name = ((QualifiedName) expression).getQualifier().resolveTypeBinding().getQualifiedName() + "." + ((QualifiedName) expression).getName().getIdentifier();
                fieldAccesses.append(name + ", ");
            }
            parseExpression(methodCalls, fieldAccesses, ((QualifiedName) expression).getQualifier());
        }
    }
}
