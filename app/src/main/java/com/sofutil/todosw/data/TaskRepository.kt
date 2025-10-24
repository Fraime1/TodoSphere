package com.sofutil.todosw.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TaskRepository(private val taskDao: TaskDao) {
    
    val activeTasks: Flow<List<Task>> = taskDao.getActiveTasks()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    
    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }
    
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    suspend fun completeTask(task: Task) {
        val completedTask = task.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(completedTask)
    }
    
    suspend fun deleteAllCompletedTasks() {
        taskDao.deleteAllCompletedTasks()
    }
    
    suspend fun getStats(): TaskStats {
        val currentTime = System.currentTimeMillis()
        val startOfWeek = currentTime - (7 * 24 * 60 * 60 * 1000)
        
        return TaskStats(
            totalTasks = taskDao.getTaskCount(),
            completedTasks = taskDao.getCompletedTaskCount(),
            activeTasks = taskDao.getActiveTaskCount(),
            overdueTasks = taskDao.getOverdueTaskCount(currentTime),
            completedThisWeek = taskDao.getCompletedTasksThisWeek(startOfWeek)
        )
    }
}

