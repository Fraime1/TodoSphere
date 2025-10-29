package com.sofutil.todosw.eriger.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class TodoSphereDataStore : ViewModel(){
    val todoSphereViList: MutableList<TodoSphereVi> = mutableListOf()
    private val _todoSphereIsFirstFinishPage: MutableStateFlow<Boolean> = MutableStateFlow(true)
    var todoIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var todoSphereContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var todoSphereView: TodoSphereVi

    fun todoSphereSetIsFirstFinishPage() {
        _todoSphereIsFirstFinishPage.value = false
    }
}