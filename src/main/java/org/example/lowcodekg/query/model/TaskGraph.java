package org.example.lowcodekg.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @Description 子任务依赖图
 * @Author Sherloque
 * @Date 2025/3/21 19:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskGraph {
    // 存储所有任务节点
    private Map<String, Task> tasks = new HashMap<>();
    
    // 邻接表存储任务依赖关系（key任务 -> value依赖的任务列表）
    private Map<String, Map<Task, String>> adjacencyList = new HashMap<>();
    
    /**
     * 添加任务节点
     * @param task 待添加的任务
     */
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        if (!adjacencyList.containsKey(task.getId())) {
            adjacencyList.put(task.getId(), new HashMap<>());
        }
    }
    
    /**
     * 添加任务依赖关系，并设置上下游依赖属性
     * @param sourceTaskId 源任务ID
     * @param targetTaskId 目标任务ID（源任务依赖于目标任务）
     * @return 是否添加成功
     */
    public boolean addDependency(String sourceTaskId, String targetTaskId, String description) {
        if (!tasks.containsKey(sourceTaskId) || !tasks.containsKey(targetTaskId)) {
            return false;
        }
        Map<Task, String> dependencies = adjacencyList.get(sourceTaskId);
        Task sourceTask = tasks.get(sourceTaskId);
        Task targetTask = tasks.get(targetTaskId);
        if (!dependencies.containsKey(targetTask)) {
            dependencies.put(targetTask, description);
            // 设置上下游依赖描述
            targetTask.setUpstreamDependency(description);
            sourceTask.setDownstreamDependency(description);
        }
        return true;
    }
    
    /**
     * 获取任务的所有依赖任务
     * @param taskId 任务ID
     * @return 依赖任务列表
     */
    public Map<Task, String> getDependencies(String taskId) {
        return adjacencyList.getOrDefault(taskId, new HashMap<>());
    }
    
    /**
     * 检查是否存在循环依赖
     * @return 是否存在循环依赖
     */
    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String taskId : tasks.keySet()) {
            if (hasCycleDFS(taskId, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasCycleDFS(String taskId, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(taskId)) {
            return true;
        }
        if (visited.contains(taskId)) {
            return false;
        }
        
        visited.add(taskId);
        recursionStack.add(taskId);
        
        Map<Task, String> dependencies = adjacencyList.get(taskId);
        for (Task dependency : dependencies.keySet()) {
            if (hasCycleDFS(dependency.getId(), visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(taskId);
        return false;
    }

    /**
     * 拓扑排序
     * @return 拓扑排序后的任务列表
     * @throws IllegalStateException 如果图中存在循环依赖
     */
    public List<Task> topologicalSort() {
        if (hasCycle()) {
            throw new IllegalStateException("Graph has a cycle, topological sort is not possible.");
        }

        Set<String> visited = new HashSet<>();
        Stack<Task> stack = new Stack<>();

        for (String taskId : tasks.keySet()) {
            if (!visited.contains(taskId)) {
                topologicalSortDFS(taskId, visited, stack);
            }
        }

        List<Task> sortedTasks = new ArrayList<>();
        while (!stack.isEmpty()) {
            sortedTasks.add(stack.pop());
        }

        return sortedTasks;
    }

    private void topologicalSortDFS(String taskId, Set<String> visited, Stack<Task> stack) {
        visited.add(taskId);

        Map<Task, String> dependencies = adjacencyList.get(taskId);
        for (Task dependency : dependencies.keySet()) {
            if (!visited.contains(dependency.getId())) {
                topologicalSortDFS(dependency.getId(), visited, stack);
            }
        }

        stack.push(tasks.get(taskId));
    }
}
