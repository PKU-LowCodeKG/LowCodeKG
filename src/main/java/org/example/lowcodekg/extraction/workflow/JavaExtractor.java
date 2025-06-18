package org.example.lowcodekg.extraction.workflow;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.model.schema.entity.workflow.JavaProject;
import org.example.lowcodekg.common.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Java 项目解析类
 */
@Service
public class JavaExtractor extends KnowledgeExtractor {

    @Override
    public void extraction() {
        for(String filePath: this.getDataDir()) {
            JavaProject javaProject = new JavaProject(funcGenerateService);
            javaProject.init();
            javaProject.setElasticSearchService(elasticSearchService);

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
                        System.out.println("AST parsing: " + sourceFilePath);
                        javaUnit.accept(new JavaASTVisitor(javaProject, FileUtils.readFileToString(new File(sourceFilePath), "utf-8")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
            // 调用 dao 接口存储到 neo4j
            javaProject.parse(workflowRepo, javaClassRepo, javaMethodRepo, javaFieldRepo);
        }
    }
}
