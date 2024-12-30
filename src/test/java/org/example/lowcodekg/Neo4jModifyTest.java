package org.example.lowcodekg;

import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

@SpringBootTest
public class Neo4jModifyTest {

    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;

    @Test
    public void testModify() {
        JavaClassEntity classEntity = javaClassRepo.findById(Long.valueOf(1952))
                .orElse(null);
        if(!Objects.isNull(classEntity)) {
            classEntity.setDescription("modified description");
        }
        javaClassRepo.save(classEntity);
    }

    @Test
    public void createTestNode() {
        JavaMethodEntity methodEntity = new JavaMethodEntity();
        methodEntity.setName("setCreateTime");
        methodEntity.setFullName("com.example.aurora-springboot.service.impl.MomentService.setCreateTime");
        methodEntity.setProjectName("aurora-springboot");
        methodEntity.setDescription("设置当前动态记录的创建时间为动态发布时间");
        methodEntity.setContent("public void setCreateTime(Date date) {\n" +
                "momentRepository.setCreateTime(date);\n" +
                "}");
        javaMethodRepo.save(methodEntity);
    }
}
