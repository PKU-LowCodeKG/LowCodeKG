package org.example.lowcodekg;

import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.dao.neo4j.repository.JavaMethodRepo;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootTest
public class Neo4jModifyTest {

    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;
    @Autowired
    private Neo4jClient neo4jClient;

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
        // delete db
        String nodeCypher = "MATCH (n) DETACH DELETE n";
        QueryRunner runner = neo4jClient.getQueryRunner();
        runner.run(nodeCypher);

        JavaMethodEntity methodEntity = new JavaMethodEntity();
        methodEntity.setName("setCreateTime");
        methodEntity.setFullName("com.example.aurora-springboot.service.impl.MomentService.setCreateTime");
        methodEntity.setProjectName("aurora-springboot");
//        methodEntity.setDescription("设置当前动态记录的创建时间为动态发布时间");

        String str = "{\"onMounted\":\"vue\",\"ref\":\"vue\",\"useDiaStore\":\"@/stores/dia\",\"computed\":\"vue\",\"defineComponent\":\"vue\",\"useAppStore\":\"@/stores/app\"}\n";
        JSONObject jo = JSONObject.parseObject(str);
        Map<String, String> m = new HashMap<>();
        jo = new JSONObject();
        m.put("a", "1"); jo.put("a", "1");
        m.put("b", "2"); jo.put("b", "2");
        methodEntity.setDescription(jo.toJSONString());

        methodEntity.setContent("public void setCreateTime(Date date) {\n" +
                "momentRepository.setCreateTime(date);\n" +
                "}");
        javaMethodRepo.save(methodEntity);
    }

     public void createBlogExample() {

     }
}
