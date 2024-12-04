package org.example.lowcodekg;

import org.example.lowcodekg.dao.neo4j.entity.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SpringBootTest
public class Neo4jModifyTest {

    @Autowired
    private JavaClassRepo javaClassRepo;

    @Test
    public void testModify() {
        JavaClassEntity classEntity = javaClassRepo.findById(Long.valueOf(1952))
                .orElse(null);
        if(!Objects.isNull(classEntity)) {
            classEntity.setDescription("modified description");
        }
        javaClassRepo.save(classEntity);
    }
}
