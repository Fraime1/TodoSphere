package com.sofutil.todosw.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val deadline: Long? = null, // Timestamp in milliseconds
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class TaskPriority {
    LOW,    // Far orbit
    MEDIUM, // Middle orbit
    HIGH    // Close orbit
}

data class TaskStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val activeTasks: Int = 0,
    val overdueTasks: Int = 0,
    val completedThisWeek: Int = 0
)

