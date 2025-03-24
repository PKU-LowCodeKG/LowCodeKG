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
    private Map<String, Task> tasks;
    
    // 邻接表存储任务依赖关系（key任务 -> value依赖的任务列表）
    private Map<String, List<Task>> adjacencyList;
    
    /**
     * 添加任务节点
     * @param task 待添加的任务
     */
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        if (!adjacencyList.containsKey(task.getId())) {
            adjacencyList.put(task.getId(), new ArrayList<>());
        }
    }
    
    /**
     * 添加任务依赖关系
     * @param sourceTaskId 源任务ID
     * @param targetTaskId 目标任务ID（源任务依赖于目标任务）
     * @return 是否添加成功
     */
    public boolean addDependency(Long sourceTaskId, Long targetTaskId) {
        if (!tasks.containsKey(sourceTaskId) || !tasks.containsKey(targetTaskId)) {
            return false;
        }
        List<Task> dependencies = adjacencyList.get(sourceTaskId);
        Task targetTask = tasks.get(targetTaskId);
        if (!dependencies.contains(targetTask)) {
            dependencies.add(targetTask);
        }
        return true;
    }
    
    /**
     * 获取任务的所有依赖任务
     * @param taskId 任务ID
     * @return 依赖任务列表
     */
    public List<Task> getDependencies(Long taskId) {
        return adjacencyList.getOrDefault(taskId, new ArrayList<>());
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
        
        List<Task> dependencies = adjacencyList.get(taskId);
        for (Task dependency : dependencies) {
            if (hasCycleDFS(dependency.getId(), visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(taskId);
        return false;
    }
}
