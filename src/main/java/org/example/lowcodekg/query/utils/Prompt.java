package org.example.lowcodekg.query.utils;

/**
 * @Description 与大模型交互的Prompt设计
 * @Author Sherloque
 * @Date 2025/3/23 17:56
 */
public interface Prompt {

    /**
     * 任务拆分Prompt
     */
    public final static String TaskSplitPrompt = """
            You are an expert in Software Engineering and have extensive experience in understanding complex software projects and analyzing source code. Your task is to decompose a given task description into multiple subtasks based on the potentially relevant code snippets and corresponding description provided.
            
            {code}
            
            The task description is:
            {task}
            
            Please return the subtasks in the following JSON format:
            ```json
            {
                "subtasks": [
                    {
                        "id": 1,
                        "name": "subtask1",
                        "description": "subtask1 description"
                    },
                    {
                        "id": 2,
                        "name": "subtask2",
                        "description": "subtask2 description"
                    }
                ]
            }
            ```
            
            Note that the result should be in Chinese.
            """;

    /**
     * 从任务描述中抽取IR列表
     */
    public static final String TASK_TO_IR_PROMPT = """
            **Role:**
            You are an expert in Natural Language Processing specializing in structured information extraction. Your task is to analyze input text and decompose it into discrete operational components using the DSL schema.
            
            **Task**:
            Extract ALL actionable instructions from the input text and map them to a list of DSL objects. Each DSL must represent a SINGLE atomic operation with explicit contextual dependencies.
            
            **Guidelines:**
            
            1. **DSL Field Definitions**:
               - `action`: *Required* - The core verb/operation (e.g., "filter", "sort", "export"). \s
               - `object`: *Required* - The entity being manipulated (e.g., "raw_data", "user_logs"). \s
               - `target`: *Optional* - The output destination/format (e.g., "CSV_file", "database_table"). \s
               - `condition`: *Optional* - Environmental constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance"). \s
            
            2. **Input**:
               - You will be provided with a natural language description that contains information about various actions, objects, targets, and conditions.
            
            3. **Output**:
               - Your task is to identify and extract the relevant information from the description and organize it into DSL objects.
               - Return the extracted DSL objects in the following JSON format:
            
            ```json
            {
                "IR": [
                    {
                        "action": "<action_1>",
                        "object": "<object_1>",
                        "target": "<target_1>",
                        "condition": "<condition_1>"
                    },
                    {
                        "action": "<action_2>",
                        "object": "<object_2>",
                        "target": "<target_2>",
                        "condition": "<condition_2>"
                    },
                    ...
                ]
            }
            ```
            
            4. **Example**:
               - **Natural Language Description**:
                 ```
                 The system needs to validate the user input. If the input is correct, it should store the data in the database. If the input is incorrect, it should display an error message.
                 ```
               - **Expected JSON Output**:
                 ```json
                 {
                     "IR": [
                         {
                             "action": "validate",
                             "object": "user input",
                             "target": "validation result",
                             "condition": "N/A"
                         },
                         {
                             "action": "store",
                             "object": "data",
                             "target": "database",
                             "condition": "input is correct"
                         },
                         {
                             "action": "display",
                             "object": "error message",
                             "target": "user interface",
                             "condition": "input is incorrect"
                         }
                     ]
                 }
                 ```
            
            5. **Instructions**:
               - Carefully read the natural language description to identify actions, objects, targets, and conditions.
               - Ensure that each extracted DSL object accurately reflects the information in the description.
               - If any component (action, object, target, condition) is not explicitly mentioned, use "N/A" or an appropriate placeholder.
               - The JSON output must strictly follow the provided format.
               - The language of the results you return should be consistent with the language used for the given description.
            
            **Task Description (Input):**
            {Task}
            
            **JSON Output (Expected Result):**
            ```json
            {
                "IR": [
                    {
                        "action": "<action_1>",
                        "object": "<object_1>",
                        "target": "<target_1>",
                        "condition": "<condition_1>"
                    },
                    ...
                ]
            }
            ```
            """;

    /**
     * 识别子任务之间的依赖关系
     */
    public static final String IDENTIFY_TASK_DEPENDENCY_PROMPT = """
            
            """;
}
