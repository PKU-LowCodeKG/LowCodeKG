package org.example.lowcodekg.service;

import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class PromptTest {
    @Autowired
    private LLMService llmService;
    @Autowired
    private IRGenerate irGenerate;
    @Autowired
    private TaskMatching taskMatch;


    @Test
    public void testLLM() {
        String prompt = "hi there";
        System.out.println(llmService.chat(prompt));
    }

    @Test
    void test() {
        String prompt = """
                ## **Role & Objective**
                You are an expert in **Software Engineering** with extensive experience in analyzing **functional implementations** in software projects. Your primary role is twofold:
                1. **Break down a given requirement** into **functional implementation-oriented subtasks** based on the provided code context. Focus specifically on **core functional components**, avoiding technical details or non-functional aspects.
                2. **Identify potential dependencies** between these subtasks, explaining why the dependencies exist based on logical, data, or workflow relationships.
            
                ---
            
                ## **Step 1: Functional Breakdown**
                Analyze the given requirement and extract **functional subtasks**
                ### **Task Categories Explanation:**
                1. data: Refers to entity fields/data structure/domain object changes (e.g., database schema changes, or DTOs).
                2. page: Involves UI/interface components (e.g., API endpoints, frontend pages, or user interactions).
                3. workflow: Represents business logic, processes, or operational sequences (e.g., service methods, validation rules, or state transitions).
            
                **Example Breakdown for "Pin Blog Post":**
                    [page] Add pin toggle button in post editor (admin)
                    [page] Display pinned badge (e.g., "Pinned") in post listings
                    [data] Add is_pinned boolean field to blog post entity
                    [data] Update database schema to persist pin status
                    [workflow] Create service method to toggle pin/unpin status
                    [workflow] Add validation (e.g., max pinned posts allowed)
                    [workflow] Implement sorting logic (pinned posts first)
            
                ### **Negative Examples (to avoid):**
                ✗ Cache handling | ✗ Test case writing | ✗ Performance optimization | ✗ Deployment configuration | ✗ Monitoring logs
            
                ---
            
                ## **Step 2: Identify Dependencies**
                Once the subtasks are extracted, analyze their relationships to determine **dependencies** based on the following criteria:
                1. **Semantic Relationships**: Identify references to **shared data, processes, or sequential operations** in natural language descriptions.
                2. **IR Sequence Logic**: If provided, analyze structured intermediate representations (`DslList`) to track how **objects and outputs are consumed across tasks**.
                3. **Development Workflow**: Consider logical execution order based on **data processing, API dependencies, UI flows**, etc.
            
                ---
            
                ## **Input Content**
            
                **Requirement Description:**
                我想要设置博客置顶状态，并添加对博客评论审核的功能
            
                **Provided Potentially Relevant Code:**
                [{"name":"updateTop","description":"该代码片段定义了一个用于更新博客置顶状态的接口。通过接收博客ID和是否置顶的状态参数，调用服务层的方法来更新指定博客的置顶状态，并返回操作成功的消息。"},{"name":"postComment","description":"该代码实现了一个博客评论提交的功能，包括验证评论内容的有效性、判断是否允许评论（根据页面状态和文章密码保护）、处理博主和访客的不同评论逻辑、设置评论属性（如IP地址、头像等）、保存评论以及发送通知邮件给相关用户。"},{"name":"blogs","description":"该代码片段定义了一个用于按置顶和创建时间排序的分页查询博客简要信息列表的功能。通过接收页码参数，调用服务层方法获取相应页面的博客信息，并返回包含这些信息的结果对象。"},{"name":"updateVisibility","description":"该代码片段定义了一个用于更新博客可见性状态的API接口。通过接收博客ID和新的可见性设置，调用服务层的方法来更新数据库中的博客可见性，并返回操作成功的消息。"},{"name":"updateComment","description":"该代码片段定义了一个用于修改博客评论的功能。它接收一个包含评论信息的实体对象，首先检查必要的字段是否为空，如果为空则返回错误信息；否则调用服务层的方法更新评论，并返回成功信息。"},{"name":"updateAll","description":"该代码片段定义了一个用于更新站点配置信息的API接口。它接收一个包含站点设置和要删除的配置ID的映射对象，然后调用服务层的方法来处理这些更新操作，并返回一个成功的响应结果。"},{"name":"updateMoment","description":"该代码片段定义了一个用于更新博客动态的接口。当接收到一个包含动态信息的请求时，如果动态的创建时间为空，则设置当前时间为创建时间；然后调用服务层的方法来更新动态，并返回一个成功的响应结果。"},{"name":"updateBlog","description":"该代码片段定义了一个用于更新博客文章的API接口。它接收一个包含博客信息的数据传输对象（DTO），验证输入参数的有效性，处理分类和标签的相关操作，并根据请求类型执行添加或更新博客的操作。"},{"name":"updatePublished","description":"该代码片段定义了一个用于更新评论公开状态的API接口。通过接收评论ID和公开状态参数，调用服务层的方法来更新指定评论的公开状态，并返回操作成功的消息。"},{"name":"blogIdAndTitle","description":"该代码片段定义了一个控制器方法，用于获取所有博客的ID和标题，以便在评论分类中进行选择。通过调用`blogService.getIdAndTitleList()`方法获取博客列表，并返回一个包含成功消息和博客数据的结果对象。"},{"name":"Comment","description":"该代码定义了一个名为Comment的类，用于表示博客文章中的评论。它包含了评论的各种属性信息，如昵称、邮箱、内容、头像、创建时间等，并且可以关联到所属的文章以及回复该评论的其他评论。"},{"name":"SiteSetting","description":"该代码定义了一个名为SiteSetting的Java类，用于表示站点设置。它包含站点设置的各种属性，如ID、英文名称、中文名称、值和类型等。"},{"name":"Tag","description":"定义了一个表示博客标签的类，包含标签的ID、名称、颜色以及关联的博客文章列表。"},{"name":"Category","description":"定义了一个表示博客分类的类，包含分类ID、名称以及属于该分类的博客文章列表。"},{"name":"Moment","description":"该代码定义了一个名为Moment的类，用于表示博客动态。它包含动态内容、创建时间、点赞数量以及是否公开等属性。"},{"name":"Blog","description":"该代码定义了一个博客文章的实体类，包含文章的基本信息、作者、分类和标签等属性。"},{"name":"CityVisitor","description":"该代码定义了一个名为CityVisitor的类，用于表示城市的访客信息。核心逻辑是存储和获取城市名称及其独立访客数量。"},{"name":"QqResultVO","description":"该代码定义了一个名为QqResultVO的Java类，用于封装API调用的结果。它包含表示成功状态、消息信息、数据内容、时间戳和API版本号的字段。"},{"name":"Visitor","description":"该代码定义了一个访客记录类，用于存储和管理访客的基本信息，包括标识码、IP地址、操作系统、浏览器等，并记录首次访问时间和最后访问时间以及访问页数统计。"},{"name":"User","description":"该代码定义了一个用户实体类，实现了Spring Security的UserDetails接口。它包含用户的详细信息（如ID、用户名、密码等），并提供了用户权限和账户状态的相关方法实现。"}]
            
                ---
            
                ## **Expected Output Format (in Chinese)**
                ```json
                {
                    "subtasks": [
                        {
                            "id": 1,
                            "name": "子任务1",
                            "category": ["data"]
                            "description": "子任务1的详细描述"
                        },
                        {
                            "id": 2,
                            "name": "子任务2",
                            "category": ["page", "workflow"]
                            "description": "子任务2的详细描述"
                        },
                        ...
                    ],
                    "dependencies": [
                        {
                            "taskId1": "taskId2",
                            "dependency": "task2 需要使用 task1 产生的计算结果"
                        },
                        {
                            "taskId3": "taskId4",
                            "dependency": "task3 依赖 task4 生成的输入数据"
                        },
                        ...
                    ]
                }
                ```
                ---
                ### **Additional Guidelines:**
                - Output must be in Chinese.
                - Each subtask should focus on a **single functional component**.
                - For task category, choose one or more of the three categories “data”, “workflow” and “page”.
                - **No specific code details** in the subtask descriptions.
                - Exclude non-functional requirements.
            
                - Dependencies should be determined strictly based on **the provided criteria**.
                - Always return **task IDs** (not names).
                - If **no dependencies exist**, return: `"Dependencies": []`
                - Explanations must be **clear, precise, and in Chinese**.
            """;

        long startTime1 = System.currentTimeMillis();
        String answer = FormatUtil.extractJson(llmService.chat(prompt));
        System.out.println(answer);
        long endTime1 = System.currentTimeMillis();
        System.out.println("llmService.generateAnswer(prompt) 执行时间: " + (endTime1 - startTime1) + " ms");
    }

    @Test
    void testTransCost() {
        List<IR> taskList = new ArrayList<>(
                List.of(
                        new IR("更新", "博客实体字段", "null", "null", null),
                        new IR("添加或修改", "布尔字段", "博客实体类", "表示置顶状态", null)
                )
        );
        List<IR> nodeList = new ArrayList<>(
                List.of(
                        new IR("null", "Blog", "null", "该代码定义了一个博客文章的实体类，包含文章的基本信息、作者、分类和标签等属性。", null)
                )
        );
        List<IR> nodeList1 = new ArrayList<>(
                List.of(
                        new IR("验证", "分类名称", "null", "如果分类名称为空", null),
                        new IR("检查", "分类名称", "null", "不包括自身", null),
                        new IR("执行更新操作", "分类实体", "null", "如果分类名称有效且不存在重复", null)
                )
        );

        // 目标：第一个结果更小
        System.out.println(taskMatch.minTransformCost(taskList, nodeList));
        System.out.println(taskMatch.minTransformCost(taskList, nodeList1));
    }
}
