package org.example.lowcodekg.extraction;

import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaFieldEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaMethodEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.*;
import org.example.lowcodekg.model.dao.neo4j.repository.*;
import org.example.lowcodekg.query.model.Node;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.Objects;
import java.util.Optional;

@SpringBootTest
public class Neo4jModifyTest {

    @Autowired
    private JavaClassRepo javaClassRepo;
    @Autowired
    private JavaMethodRepo javaMethodRepo;
    @Autowired
    private JavaFieldRepo javaFieldRepo;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private ComponentRepo componentRepo;
    @Autowired
    private ConfigItemRepo configItemRepo;
    @Autowired
    private ScriptRepo scriptRepo;
    @Autowired
    private ScriptMethodRepo scriptMethodRepo;
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
//        String nodeCypher = "MATCH (n) DETACH DELETE n";
//        QueryRunner runner = neo4jClient.getQueryRunner();
//        runner.run(nodeCypher);
        ConfigItemEntity ci1 = new ConfigItemEntity();
        configItemRepo.save(ci1);

//        JavaMethodEntity methodEntity = new JavaMethodEntity();
//        methodEntity.setName("setCreateTime");
//        methodEntity.setFullName("com.example.aurora-springboot.service.impl.MomentService.setCreateTime");
//        methodEntity.setProjectName("aurora-springboot");
////        methodEntity.setDescription("设置当前动态记录的创建时间为动态发布时间");
//
//        String str = "{\"onMounted\":\"vue\",\"ref\":\"vue\",\"useDiaStore\":\"@/stores/dia\",\"computed\":\"vue\",\"defineComponent\":\"vue\",\"useAppStore\":\"@/stores/app\"}\n";
//        JSONObject jo = JSONObject.parseObject(str);
//        Map<String, String> m = new HashMap<>();
//        jo = new JSONObject();
//        m.put("a", "1"); jo.put("a", "1");
//        m.put("b", "2"); jo.put("b", "2");
//        methodEntity.setDescription(jo.toJSONString());
//
//        methodEntity.setContent("public void setCreateTime(Date date) {\n" +
//                "momentRepository.setCreateTime(date);\n" +
//                "}");
//        javaMethodRepo.save(methodEntity);
    }

    @Test
     public void createBlogExample() {
         // delete db
         String nodeCypher = "MATCH (n) DETACH DELETE n";
         QueryRunner runner = neo4jClient.getQueryRunner();
         runner.run(nodeCypher);

         PageEntity p1 = new PageEntity();
         p1.setName("WriteMoment");
         p1 = pageRepo.save(p1);
         ComponentEntity c1 = new ComponentEntity();
         c1.setName("el-form");
         c1 = componentRepo.save(c1);
         pageRepo.createRelationOfContainedComponent(p1.getId(), c1.getId());
         ComponentEntity c2 = new ComponentEntity();
         c2.setName("动态内容");
         c2 = componentRepo.save(c2);
         componentRepo.createRelationOfChildComponent(c1.getId(), c2.getId());
         ComponentEntity c3 = new ComponentEntity();
         c3.setName("el-form-item");
         c3 = componentRepo.save(c3);
         componentRepo.createRelationOfChildComponent(c1.getId(), c3.getId());
         ComponentEntity c4 = new ComponentEntity();
         c4.setName("发布动态");
         c4 = componentRepo.save(c4);
         componentRepo.createRelationOfChildComponent(c3.getId(), c4.getId());
         ConfigItemEntity ci1 = new ConfigItemEntity();
         ci1.setName("click");
         ci1 = configItemRepo.save(ci1);
         componentRepo.createRelationOfContainedConfigItem(c4.getId(), ci1.getId());
         ScriptEntity s1 = new ScriptEntity();
         s1.setName("writeMoment");
         s1 = scriptRepo.save(s1);
         ScriptMethodEntity sm1 = new ScriptMethodEntity();
         sm1.setName("saveMoment");
         sm1 = scriptMethodRepo.save(sm1);
         scriptRepo.createRelationOfContainedMethod(s1.getId(), sm1.getId());
         configItemRepo.createRelationOfRelatedMethod(ci1.getId(), sm1.getId());
         pageRepo.createRelationOfContainedScript(p1.getId(), s1.getId());

         JavaClassEntity jc1 = new JavaClassEntity();
         jc1.setName("MomentController");
         jc1 = javaClassRepo.save(jc1);
         JavaFieldEntity jf1 = new JavaFieldEntity();
         JavaMethodEntity jm1 = new JavaMethodEntity();
         jm1.setName("newMoment");
         jm1 = javaMethodRepo.save(jm1);
         javaClassRepo.createRelationOfMethod(jc1.getId(), jm1.getId());
         JavaMethodEntity jm2 = new JavaMethodEntity();
         jm2.setName("writeMoment");
         jm2 = javaMethodRepo.save(jm2);
         jf1.setName("momentService");
         jf1 = javaFieldRepo.save(jf1);
         javaMethodRepo.createRelationOfMethodCall(jm1.getId(), jm2.getId());
         javaClassRepo.createRelationOfField(jc1.getId(), jf1.getId());
         JavaClassEntity jc2 = new JavaClassEntity();
         jc2.setName("MomentService");
         jc2 = javaClassRepo.save(jc2);
         javaFieldRepo.createRelationOfFieldType(jf1.getId(), jc2.getId());
         javaClassRepo.createRelationOfMethod(jc2.getId(), jm2.getId());
         JavaClassEntity jc3 = new JavaClassEntity();
         jc3.setName("Moment");
         jc3 = javaClassRepo.save(jc3);
         javaClassRepo.setDataObjectLabel(jc3.getId());
         javaMethodRepo.createRelationOfParamType(jm2.getId(), jc3.getId());

         scriptMethodRepo.createRelationOfMethod(sm1.getId(), jm1.getId());
     }

     @Test
     public void testIR() {
        Optional<PageEntity> entity = pageRepo.findById(145L);
        if(entity.isPresent()) {
            PageEntity pageEntity = entity.get();
            Node node = new Node(pageEntity);
            System.out.println(node.getIrList());
        }
     }
}
