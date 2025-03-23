package org.example.lowcodekg.query.utils;

/**
 * @Description 与大模型交互的Prompt设计
 * @Author Sherloque
 * @Date 2025/3/23 17:56
 */
public interface Prompt {

    public final static String TaskSplitPrompt = """
            You are a helpful assistant.
            You are given a task description, and you need to split it into subtasks based on the following potentially relevant code snippets.
            {code}
            
            The task description is:
            {task}
            
            You need to return the subtasks in a JSON format, with the following format:
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
            
            Note that the result should be Chinese.
            """;
}
