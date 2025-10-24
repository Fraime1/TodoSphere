package com.sofutil.todosw.eriger.presentation.ui.view

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class TodoSphereDataStore : ViewModel(){
    val todoSphereViList: MutableList<TodoSphereVi> = mutableListOf()
    private val _todoSphereIsFirstFinishPage: MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun todoSphereSetIsFirstFinishPage() {
        _todoSphereIsFirstFinishPage.value = false
    }
}