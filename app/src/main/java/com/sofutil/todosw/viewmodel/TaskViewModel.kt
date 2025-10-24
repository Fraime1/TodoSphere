package com.sofutil.todosw.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sofutil.todosw.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: TaskRepository
    private val settingsDataStore: SettingsDataStore
    
    val activeTasks: StateFlow<List<Task>>
    val stats: StateFlow<TaskStats>
    val theme: StateFlow<String>
    
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()
    
    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        settingsDataStore = SettingsDataStore(application)
        
        activeTasks = repository.activeTasks
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        stats = flow {
            while (true) {
                emit(repository.getStats())
                kotlinx.coroutines.delay(1000)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskStats()
        )
        
        theme = settingsDataStore.theme
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "Dark"
            )
    }
    
    fun loadTask(taskId: Int) {
        viewModelScope.launch {
            _selectedTask.value = repository.getTaskById(taskId)
        }
    }
    
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }
    
    fun completeTask(task: Task) {
        viewModelScope.launch {
            repository.completeTask(task)
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsDataStore.setTheme(theme)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationsEnabled(enabled)
        }
    }
}

