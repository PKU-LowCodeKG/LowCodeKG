package org.example.lowcodekg.query.utils;

/**
 * @Description 与大模型交互的Prompt设计
 * @Author Sherloque
 * @Date 2025/3/23 17:56
 */
public interface Prompt {

    /**
     * 总结工作流功能信息
     */
    public static final String WORKFLOW_SUMMARIZE_PROMPT = """
            You are an expert in software development and code analysis. 
            Your task is to analyze a given code snippet and provide a **concise and brief** summary of its functionality in Chinese. 
            
            Here is the code snippet for analysis:
            {code}
            
            Present your summary in the following JSON format(in Chinese):
            ```json
            {
                "functionality": ""
            }
            ```
            
            **Guidelines**
            - Focus on the core logic and purpose of the code, and avoid specific code implementation details.
            - Make sure the summary is in Chinese and as short as possible.
            """;

    /**
     * 总结页面功能信息
     */
    public static final String PAGE_SUMMARIZE_PROMPT = """
            You are an expert in software development and code analysis. 
            Your task is to analyze a given code snippet in a **Front-End** project and provide a **concise and brief** summary of its functionality in Chinese. 
            
            Here is the code snippet for analysis:
            {code}
            
            In your analysis, there may be some keywords provided to help you better understand the context and focus of the code functionality:
            {keywords}. 
            
            Present your summary in the following JSON format(in Chinese):
            ```json
            {
                "functionality": ""
            }
            ```
            
            **Guidelines**
            - Focus on the core logic and purpose of the code, and avoid specific code implementation details.
            - Make sure the summary is in Chinese and as short as possible.
            """;

    /**
     * 需求分解，识别子任务依赖
     */
    public final static String TASK_GRAPH_BUILD_PROMPT = """
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
            {task} 
            
            **Provided Potentially Relevant Code:** 
            {code}
            
            ---
            
            ## **Expected Output Format (in Chinese)**
            ```json
            {
                "subtasks": [
                    {
                        "id": 1,
                        "name": "子任务1",
                        "category": ["data", "page", "workflow"]
                        "description": "子任务1的详细描述"
                    },
                    {
                        "id": 2,
                        "name": "子任务2",
                        "category": ["data", "page", "workflow"]
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

    /**
     * 任务拆分Prompt
     */
    public final static String TaskSplitPrompt = """
            You are an expert in Software Engineering with extensive experience in analyzing functional implementations in software projects.
            
            Your task is to break down the given requirement into functional implementation-oriented subtasks based on the provided code context. Focus specifically on core functional components rather than technical details or non-functional aspects.
            
            **Key Focus Areas:**
                1.Entity fields/data structure changes (e.g.,adding/modifying database fields)
                2.Service layer method implementation (e.g.,creating/updating domain service methods)
                3.Interface/API definition (e.g.,adding REST endpoints)
                4.Business logic flow (e.g.,state change rules)
                5.Domain object relationship adjustments
            
            **Negative Examples (to avoid):**
            × Cache handling × Test case writing × Performance optimization × Deployment configuration × Monitoring logs
            
            **Example Breakdown for "Pin Blog Post":**
                1.Add an "is pinned"boolean field to the blog entity
                2.Create a blog update service method to handle pinning status
                3.Implement persistence of pinning status to the database
                4.Add an interface for querying the pinning status of a post
            
            ### **Input**
            
            **Requirement Description:**
            {task}
            
            **Provided Potentially Relevant Code:**
            {code}
            
            ### **Output Format (in Chinese):**
            Please return the breakdown results in the following JSON format:
            ```json
            {
                "subtasks": [
                    {
                        "id": 1,
                        "name": "子任务1",
                        "description": "子任务1的详细描述"
                    },
                    {
                        "id": 2,
                        "name": "子任务2",
                        "description": "子任务2的详细描述"
                    }
                ],
                "Dependencies": [
                    {
                        "taskId1": "taskId2",
                        "dependency": "task2 需要使用 task1 产生的计算结果"
                    },
                    {
                        "taskId3": "taskId4",
                        "dependency": "task3 依赖 task4 生成的输入数据"
                    }
                ]
            }
            ```
            
            Ensure:
            - Output in Chinese
            - Each subtask focuses on a single functional component
            - No specific code details in the description of subtask
            - Excludes non-functional requirements
            """;

    /**
     * 从任务描述中抽取IR列表
     */
    public static final String TASK_TO_IR_PROMPT = """
            You are an expert in Natural Language Processing specializing in structured information extraction. 
            Your task is to analyze input text and decompose it into discrete operational functional components using the DSL schema.
            
            **Task**:
            Extract ALL actionable instructions from the input text and map them to a list of DSL objects. Each DSL represents a **SINGLE FUNCTIONAL** atomic operation.
            
            **DSL Schema**:
               - `action`: indicates what function it requires to perform, which is the core operation of the task, such as get, convert, or create..
               - `object`: indicates what it operates on, which is the primary entity of the task, such as data types, libraries, or frameworks.
               - `target`: indicates what it wants to achieve, which is the result of the task.
               - `condition`: indicates any rules or restrictions associated with the task such as specific programming languages (e.g., in Java) or data formats (e.g., CSV-format input data).
            
            **Input**:
            You will be provided with a natural language description that describes a functional requirement, the task description is:
            {Task}
            
            **Output**:
            Return the extracted DSL objects in the following JSON format in Chinese:
            
            ```json
            {
                "IR": [
                    {
                        "action": "<动作1>",
                        "object": "<对象1>",
                        "target": "<目标1>",
                        "condition": "<条件1>"
                    },
                    {
                        "action": "<操作2>",
                        "object": "<对象2>",
                        "target": "<目标2>",
                        "condition": "<条件2>"
                    },
                    ...
                ]
            }
            ```
            
            **Instructions**:
               - The input may contain pieces(e.g., stopwords) that are not relevant to the task, exclude such content from your generation.
               - If any component (action, object, target, condition) is not mentioned, use "null" as placeholder.
               - The output must strictly follow the provided JSON format, and be presented in Chinese.
            """;

    /**
     * 从资源描述中抽取IR列表
     */
    public static final String RESOURCE_TO_IR_PROMPT = """
            You are an expert in Natural Language Processing specializing in structured information extraction. 
            Your task is to analyze input text or code and decompose it into discrete operational components using the DSL schema.
           
            **Task**:
            Extract ALL actionable instructions from the input text or code and map them to a list of DSL objects. Each DSL must represent a SINGLE atomic operation with explicit contextual dependencies.
           
            **DSL Field Definitions**:
               - `action`: *Required* - The core verb/operation (e.g., "filter", "sort", "export").
               - `object`: *Required* - The entity being manipulated (e.g., "raw_data", "user_logs").
               - `target`: *Optional* - The output destination/format (e.g., "CSV_file", "database_table").
               - `condition`: *Optional* - Environmental constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance").
          
            **Task Description (Input):**
            {Task}
            
            **Output**:
               - Your task is to identify and extract the relevant information from the description and organize it into DSL objects.
               - Return the extracted DSL objects in the following JSON format in Chinese:
               
             ```json
            {
                "IR": [
                    {
                        "action": "<动作1>",
                        "object": "<对象1>",
                        "target": "<目标1>",
                        "condition": "<条件1>"
                    },
                    {
                        "action": "<操作2>",
                        "object": "<对象2>",
                        "target": "<目标2>",
                        "condition": "<条件2>"
                    },
                    ...
                ]
            }
            ```
           
            **Instructions**:
               - Carefully read the natural language description to identify actions, objects, targets, and conditions.
               - Ensure that each extracted DSL object accurately reflects the information in the description.
               - If any component (action, object, target, condition) is not explicitly mentioned, use "null" as placeholder.
               - The JSON output must strictly follow the provided format in Chinese.
            """;

    /**
     * 识别子任务之间的依赖关系
     */
    public static final String IDENTIFY_TASK_DEPENDENCY_PROMPT = """
            ## AI Assistant's Role
            You are an intelligent analytical assistant designed to identify potential dependencies between subtasks in a software development context. Your task is to analyze the provided subtasks and their details, determine dependencies between them, and explain why these dependencies exist.
            
            ## Input Format
            - **Overall Task Description**: A comprehensive summary of the entire task, derived from user requirements or product specifications.
            - **Subtask Details**: A list of subtasks, each described in natural language and structured as follows:
              - `id`: A unique identifier for the subtask.
              - `name`: The name of the subtask.
              - `description`: A detailed explanation of what the subtask accomplishes, typically based on user stories or technical specifications.
              - `DslList`: A list of intermediate representations (IRs) for the subtask, each containing:
                - `action` (Required): The core operation (e.g., "filter", "sort", "export").
                - `object` (Required): The entity being manipulated (e.g., "raw_data", "user_logs").
                - `target` (Optional): The output destination or format (e.g., "CSV_file", "database_table").
                - `condition` (Optional): Constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance").
            
            ## Dependency Identification Criteria
            To determine dependencies between subtasks, consider the following factors:
            1. **Semantic Relationships**: Analyze the natural language descriptions to identify references to shared data, processes, or sequential operations.
            2. **IR Sequence Logic**: Examine the `DslList` fields, especially how `object` and `target` may indicate dependencies where one subtask produces an output that another subtask consumes.
            3. **Development Workflow**: Consider typical software development workflows, such as data processing pipelines, API integrations, or user interface sequences, to identify logical dependencies.
            
            ## Input Content
            The overall task description is:
            {query}
            
            And the detail of subtasks is as follow:
            {subTasks}
           
            ## Expected Output
            Your response should be a JSON object listing dependent subtask pairs, with explanations in Chinese. The format is:
            ```json
            {
                "Dependencies": [
                    {
                        "taskId1": "taskId2",
                        "dependency": "task2 需要使用 task1 产生的计算结果"
                    },
                    {
                        "taskId3": "taskId4",
                        "dependency": "task3 依赖 task4 生成的输入数据"
                    }
                ]
            }
            ```
            
            ### Additional Guidelines
            - The dependencies should be determined strictly based on the provided criteria.
            - Always return task **IDs**, not names.
            - If no dependencies are found, return an empty list (`"Dependencies": []`).
            - Explanations should be clear, precise, and written in **Chinese**.
            """;

    /**
     * 判断子任务的检索对象类型
     */
    public static final String TYPE_OF_RETRIEVED_ENTITY_PROMPT = """
            You are an experienced software engineer with expertise in analyzing development tasks and identifying relevant system objects. 
            
            ### Task Overview 
            You will be provided with: 
            1. A task description that outlines a specific development-related goal. 
            2. A sequence of operations that detail the steps involved in executing the task. 
            
            ### Your Objective 
            Your job is to determine which types of objects need to be retrieved for this task. The possible object types are: 
            - **Page**: Represents a UI page or interface that users interact with. 
            - **Workflow**: Represents a predefined sequence of steps or processes executed in the system. 
            - **DataObject**: Represents structured data entities that store and manage information. 
            
            Carefully analyze both the task description and the sequence of operations to determine which of the above object types are required. The task may involve multiple object types. 
            
            ### Input Content
            {Task}
            
            ### Response Format 
            Return your answer as a JSON object with boolean values indicating whether each object type is relevant: 
            
            ```json
            {
              "Page": true/false,
              "Workflow": true/false,
              "DataObject": true/false
            }
            
            Ensure your classification is based strictly on the provided information. If the task does not explicitly or implicitly require a certain object type, mark it as false.
            """;

    /**
     * 根据任务的上下游依赖对检索结果进行初步筛选
     */
    public static final String FILTER_BY_DEPENDENCY_PROMPT = """
            You are tasked with filtering a list of retrieved resources based on the dependencies of a software development task. 
            Your goal is to retain only the resources that are directly relevant to the task's **upstream and downstream dependencies**. 
            
            ## **Input Data:** 
            1. **Task Description**: A description of the user's software development task. 
            2. **Upstream Dependencies**: Tasks that must be completed before this task can begin. 
            3. **Downstream Dependencies**: Tasks that depend on the completion of this task. 
            4. **Retrieved Resources**: A list of resources retrieved for the task, formatted as follows: 
                Resource { id="", name="", label="", content="", description="" }     
           
            ## **Instructions:** 
             - Analyze the task description, upstream dependencies, and downstream dependencies.
            - Pay special attention to the input and output types of the dependent tasks.
             - A resource is considered relevant if it meets at least one of the following conditions:
                - It directly describes, implements, or supports an upstream/downstream task.
                - It provides critical information about the inputs/outputs of dependencies.
                - It includes APIs, tools, or libraries essential to the dependencies.
             - Exclude resources that do not contribute to understanding or executing the dependencies.
             - Return only the filtered resources in the JSON format:
            
            ## Input Content
            
            ### The task Description is:
            {task}
            
            ### The upstream dependencies is(may be null):
            {upstreamDependency}
            
            ### The downstream dependencies is(may be null):
            {downstreamDependency}
            
            ### The retrieved resource list is as follows:
            {nodeList}
            
            Please return the names of reserved tasks in the following JSON format.
            ```json
            {
              "resources": [
                {
                  "name": ""
                },
                ...
              ]
            }
            ```
            """;

    /**
     * 依靠LLM对子任务检索结果重排序
     */
    public static final String RERANK_WITHIN_TASK_PROMPT = """
            **Role:** 
            You are a seasoned software engineering expert with deep technical experience. 
            
            **Task:** 
            Given a user’s functional requirement and its decomposed subtasks—each with a list of recommended resources—you must: 
            1. **Re-sort resources** for each subtask based on: 
               - Relevance to the subtask description 
               - Dependencies between subtasks (e.g., execution order, shared resources) 
            2. **Filter out irrelevant resources** (low relevance or redundancy). 
            
            
            ### **Input Format** 
            ```json
            {
                "task": "[Overall functional requirement]",
                "subTasks": [
                    {
                        "name": "[Subtask 1 name]",
                        "description": "[Subtask 1 description]",
                        "resources": [
                            {
                                "resourceName": "",
                                "resourceDescription": ""
                            },
                            ...
                        ]
                    }, 
                ...
                ]
            }
            ```
            ### **Input Content**
            {input}
            
            ### **Output** 
            - **Strictly adhere to this JSON structure:** 
              ```json 
              { 
                "[Subtask 1 name]": [ 
                  { "resourceName": "[resource name 1]" }, 
                  { "resourceName": "[resource name 2]" }, 
                  ... 
                ], 
                "[Subtask 2 name]": [ ... ], 
                ... 
              } 
              ``` 
            - **Rules for sorting/filtering:** 
              **Priority order:** 
                 - Resources directly solving the subtask’s core problem > supporting tools.
                 - Reserve resources of the DataObject type if the subtask involves data entities.
              **Dependency analysis:** 
                 - Remove duplicates (keep only the highest-priority instance). 
              **Filtering criteria:** 
                 - Exclude resources with no clear relevance to the subtask description. 
            """;
}
