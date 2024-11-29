package org.example.lowcodekg.extraction.java;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaFieldRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.schema.entity.workflow.JavaClass;
import org.example.lowcodekg.schema.entity.workflow.JavaProject;
import org.springframework.beans.factory.annotation.Autowired;
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
            JavaProject javaProject = new JavaProject();
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
            javaProject.parseRelations(javaClassRepo, javaMethodRepo, javaFieldRepo);
            javaProject.storeRelations(javaClassRepo, javaMethodRepo, javaFieldRepo);
        }


        // test
//        try {
//            System.out.println(elasticSearchService.searchEmbedding("获取用户在线列表"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


    public static void main(String[] args) {
        String path = "usr/chang/projects/LowCodeKG";
        System.out.println(path.split("/")[path.split("/").length - 1]);
    }
}
