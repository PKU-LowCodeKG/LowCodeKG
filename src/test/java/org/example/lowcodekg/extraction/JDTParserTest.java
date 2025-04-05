package org.example.lowcodekg.extraction;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaMethodRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaFieldRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.extraction.workflow.JavaASTVisitor;
import org.example.lowcodekg.model.schema.entity.workflow.JavaProject;
import org.example.lowcodekg.query.service.util.summarize.FuncGenerate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class JDTParserTest {

    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;
    @Autowired
    private JavaFieldRepo javaFieldRepo;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private FuncGenerate funcGenerate;


    @Test
    public void test() {
        String filePath = "/Users/chang/Documents/projects/data_projects/aurora/aurora-springboot";
        JavaProject javaProject = new JavaProject(funcGenerate);
        javaProject.init();

        String projectName = filePath.split("/")[filePath.split("/").length - 1];
        javaProject.setProjectName(projectName);
        Collection<File> javaFiles = FileUtils.listFiles(new File(filePath), new String[]{"java"}, true);
        Set<String> srcPathSet = new HashSet<>();
        Set<String> srcFolderSet = new HashSet<>();
        for (File javaFile : javaFiles) {
            String srcPath = javaFile.getAbsolutePath();
            String srcFolderPath = javaFile.getParentFile().getAbsolutePath();
            srcPathSet.add(srcPath);
            srcFolderSet.add(srcFolderPath);
        }
        String[] srcPaths = new String[srcPathSet.size()];
        srcPathSet.toArray(srcPaths);

        String[] srcFolderPaths = new String[srcFolderSet.size()];
        srcFolderSet.toArray(srcFolderPaths);

        ASTParser parser = ASTParser.newParser(AST.JLS10);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(null, new String[]{filePath}, new String[]{"utf-8"}, true);
        Map<String, String> options = JavaCore.getOptions();
        options.put("org.eclipse.jdt.core.compiler.source", "1.17");
        parser.setCompilerOptions(options);
        String[] encodings = new String[srcPaths.length];
        for (int i = 0; i < srcPaths.length; i++) {
            encodings[i] = "utf-8";
        }
        parser.createASTs(srcPaths, encodings, new String[]{}, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                try {
                    javaUnit.accept(new JavaASTVisitor(javaProject, FileUtils.readFileToString(new File(sourceFilePath), "utf-8")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        javaProject.parse(workflowRepo, javaClassRepo, javaMethodRepo, javaFieldRepo);
    }

    @Test
    public void testAnnotation() {
        String filePath = "/Users/chang/Documents/projects/data_projects/aurora/aurora-springboot/src/main/java/com/aurora/controller/ArticleController.java";
        try {
            String content = FileUtils.readFileToString(new File(filePath), "utf-8");
            JavaProject javaProject = new JavaProject(funcGenerate);
            ASTParser parser = ASTParser.newParser(AST.JLS10);
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setBindingsRecovery(true);
            String path = "/Users/chang/Documents/projects/data_projects/aurora/aurora-springboot";
            parser.setEnvironment(null, new String[]{path}, new String[]{"utf-8"}, true);
            Map<String, String> options = JavaCore.getOptions();
            options.put("org.eclipse.jdt.core.compiler.source", "1.17");
            parser.setCompilerOptions(options);

            Set<String> srcPathSet = new HashSet<>();
            Set<String> srcFolderSet = new HashSet<>();
            File javaFile = new File(filePath);
            String srcPath = javaFile.getAbsolutePath();
            String srcFolderPath = javaFile.getParentFile().getAbsolutePath();
            srcPathSet.add(srcPath);
            srcFolderSet.add(srcFolderPath);
            String[] srcPaths = new String[srcPathSet.size()];
            srcPathSet.toArray(srcPaths);

            String[] srcFolderPaths = new String[srcFolderSet.size()];
            srcFolderSet.toArray(srcFolderPaths);
            String[] encodings = new String[srcPaths.length];
            for (int i = 0; i < srcPaths.length; i++) {
                encodings[i] = "utf-8";
            }
            parser.createASTs(srcPaths, encodings, new String[]{}, new FileASTRequestor() {
                @Override
                public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                    try {
                        System.out.println("AST parsing: " + sourceFilePath);
                        javaUnit.accept(new JavaASTVisitor(javaProject, FileUtils.readFileToString(new File(sourceFilePath), "utf-8")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
