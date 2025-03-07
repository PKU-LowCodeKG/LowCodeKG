package org.example.lowcodekg;

import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.ConfigItemRepo;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class LowCodeKgApplicationTests {

    @Autowired
    private ComponentRepo componentRepo;

    @Autowired
    private ConfigItemRepo configItemRepo;

    @Autowired
    private ChatClient chatClient;

    @Test
    void contextLoads() {
    }

    @Test
    void promptTest() {
        String code = """
                    public class PageUtil {
                     
                         private static final ThreadLocal<Page<?>> PAGE_HOLDER = new ThreadLocal<>();
                     
                         public static void setCurrentPage(Page<?> page) {
                             PAGE_HOLDER.set(page);
                         }
                     
                         public static Page<?> getPage() {
                             Page<?> page = PAGE_HOLDER.get();
                             if (Objects.isNull(page)) {
                                 setCurrentPage(new Page<>());
                             }
                             return PAGE_HOLDER.get();
                         }
                     
                         public static Long getCurrent() {
                             return getPage().getCurrent();
                         }
                     
                         public static Long getSize() {
                             return getPage().getSize();
                         }
                     
                         public static Long getLimitCurrent() {
                             return (getCurrent() - 1) * getSize();
                         }
                     
                         public static void remove() {
                             PAGE_HOLDER.remove();
                         }
                     
                     }
                """;
        String template = """
                你是一名程序员，正在阅读一个博客系统的后端代码，
                请为下面这段代码生成一句简要的描述信息。
                
                code:
                {code}
                
                description:
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("code", code));
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
        String content = assistantMessage.getContent();
        System.out.println(content);
    }

//    @Test
//    void componentAddSearchTest() {
//        // 测试前先删除所有节点（同时也会删除相应关系）
//        componentRepo.deleteAll();
//        configItemRepo.deleteAll();
//
//        Category category = new Category();
//        category.setFunctionalityCategory(FunctionalityCategory.FORM);
//        category.setSceneLabel(SceneLabel.ECOMMERCE);
//
//        ComponentEntity componentEntity = new ComponentEntity(
//                 "单选框",
//                 "component",
//                 "用于在多个备选项中选中单个状态。");
//        ConfigItemEntity configItemEntity1 = new ConfigItemEntity(
//                "autoFocus",
//                "boolean",
//                "false",
//                "自动获取焦点");
//        ConfigItemEntity configItemEntity2 = new ConfigItemEntity(
//                "checked",
//                "boolean",
//                "false",
//                "指定当前是否选中"
//        );
//
//        componentEntity.getContainedConfigItemEntities().add(configItemEntity1);
//        componentEntity.getContainedConfigItemEntities().add(configItemEntity2);
//        System.out.println("目前component的配置项有： " + componentEntity.getContainedConfigItemEntities());
//
//        componentRepo.save(componentEntity);
//        configItemRepo.saveAll(componentEntity.getContainedConfigItemEntities());
//
//        List<ComponentEntity> componentEntities = componentRepo.findByNameContaining("单选");
//        System.out.println("查询名字包含[单选]的ComponentEntity: " + componentEntities);
//
//        List<ConfigItemEntity> configItemEntities = configItemRepo.findConfigItemsByComponentName("单选框");
//        System.out.println("查询名字为[单选框]的组件包含的ConfigItemEntity: " + configItemEntities);
//    }

}
