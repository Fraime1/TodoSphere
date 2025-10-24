package com.sofutil.todosw.eriger.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofutil.todosw.eriger.data.shar.TodoSphereSharedPreference
import com.sofutil.todosw.eriger.data.utils.TotoSphereSystemService
import com.sofutil.todosw.eriger.domain.usecases.TodoSphereGetAllUseCase
import com.sofutil.todosw.eriger.presentation.app.TodoSphereAppsFlyerState
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodoSphereLoadViewModel(
    private val todoSphereGetAllUseCase: TodoSphereGetAllUseCase,
    private val todoSphereSharedPreference: TodoSphereSharedPreference,
    private val totoSphereSystemService: TotoSphereSystemService
) : ViewModel() {

    private val _todoSphereHomeScreenState: MutableStateFlow<TodoSphereHomeScreenState> =
        MutableStateFlow(TodoSphereHomeScreenState.TodoSphereLoading)
    val todoSphereHomeScreenState = _todoSphereHomeScreenState.asStateFlow()

    private var todoSphereGetApps = false


    init {
        viewModelScope.launch {
            when (todoSphereSharedPreference.todoSphereAppState) {
                0 -> {
                    if (totoSphereSystemService.todoSphereIsOnline()) {
                        TodoSphereApp.todoSphereConversionFlow.collect {
                            when(it) {
                                TodoSphereAppsFlyerState.TodoSphereDefault -> {}
                                TodoSphereAppsFlyerState.TodoSphereError -> {
                                    todoSphereSharedPreference.todoSphereAppState = 2
                                    _todoSphereHomeScreenState.value =
                                        TodoSphereHomeScreenState.TodoSphereError
                                    todoSphereGetApps = true
                                }
                                is TodoSphereAppsFlyerState.TodoSphereSuccess -> {
                                    if (!todoSphereGetApps) {
                                        todoSphereGetData(it.todoSphereData)
                                        todoSphereGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _todoSphereHomeScreenState.value =
                            TodoSphereHomeScreenState.TodoSphereNotInternet
                    }
                }
                1 -> {
                    if (totoSphereSystemService.todoSphereIsOnline()) {
                        if (TodoSphereApp.TODO_SPHERE_FB_LI != null) {
                            _todoSphereHomeScreenState.value =
                                TodoSphereHomeScreenState.TodoSphereSuccess(
                                    TodoSphereApp.TODO_SPHERE_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > todoSphereSharedPreference.todoSphereExpired) {
                            Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Current time more then expired, repeat request")
                            TodoSphereApp.todoSphereConversionFlow.collect {
                                when(it) {
                                    TodoSphereAppsFlyerState.TodoSphereDefault -> {}
                                    TodoSphereAppsFlyerState.TodoSphereError -> {
                                        _todoSphereHomeScreenState.value =
                                            TodoSphereHomeScreenState.TodoSphereSuccess(
                                                todoSphereSharedPreference.todoSphereSavedUrl
                                            )
                                        todoSphereGetApps = true
                                    }
                                    is TodoSphereAppsFlyerState.TodoSphereSuccess -> {
                                        if (!todoSphereGetApps) {
                                            todoSphereGetData(it.todoSphereData)
                                            todoSphereGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Current time less then expired, use saved url")
                            _todoSphereHomeScreenState.value =
                                TodoSphereHomeScreenState.TodoSphereSuccess(
                                    todoSphereSharedPreference.todoSphereSavedUrl
                                )
                        }
                    } else {
                        _todoSphereHomeScreenState.value =
                            TodoSphereHomeScreenState.TodoSphereNotInternet
                    }
                }
                2 -> {
                    _todoSphereHomeScreenState.value =
                        TodoSphereHomeScreenState.TodoSphereError
                }
            }
        }
    }


    private suspend fun todoSphereGetData(conversation: MutableMap<String, Any>?) {
        val todoSphereData = todoSphereGetAllUseCase.invoke(conversation)
        if (todoSphereSharedPreference.todoSphereAppState == 0) {
            if (todoSphereData == null) {
                todoSphereSharedPreference.todoSphereAppState = 2
                _todoSphereHomeScreenState.value =
                    TodoSphereHomeScreenState.TodoSphereError
            } else {
                todoSphereSharedPreference.todoSphereAppState = 1
                todoSphereSharedPreference.apply {
                    todoSphereExpired = todoSphereData.todoSphereExpires
                    todoSphereSavedUrl = todoSphereData.todoSphereUrl
                }
                _todoSphereHomeScreenState.value =
                    TodoSphereHomeScreenState.TodoSphereSuccess(todoSphereData.todoSphereUrl)
            }
        } else  {
            if (todoSphereData == null) {
                _todoSphereHomeScreenState.value =
                    TodoSphereHomeScreenState.TodoSphereSuccess(todoSphereSharedPreference.todoSphereSavedUrl)
            } else {
                todoSphereSharedPreference.apply {
                    todoSphereExpired = todoSphereData.todoSphereExpires
                    todoSphereSavedUrl = todoSphereData.todoSphereUrl
                }
                _todoSphereHomeScreenState.value =
                    TodoSphereHomeScreenState.TodoSphereSuccess(todoSphereData.todoSphereUrl)
            }
        }
    }


    sealed class TodoSphereHomeScreenState {
        data object TodoSphereLoading : TodoSphereHomeScreenState()
        data object TodoSphereError : TodoSphereHomeScreenState()
        data class TodoSphereSuccess(val data: String) : TodoSphereHomeScreenState()
        data object TodoSphereNotInternet: TodoSphereHomeScreenState()
    }
}