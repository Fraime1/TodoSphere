package com.sofutil.todosw.eriger.presentation.app

import android.app.Application
import android.view.WindowManager
import com.sofutil.todosw.eriger.data.utils.TodoSphereAppsflyer
import com.sofutil.todosw.eriger.data.utils.TotoSphereSystemService
import com.sofutil.todosw.eriger.presentation.di.todoSphereModule
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


sealed interface TodoSphereAppsFlyerState {
    data object TodoSphereDefault : TodoSphereAppsFlyerState
    data class TodoSphereSuccess(val todoSphereData: MutableMap<String, Any>?) :
        TodoSphereAppsFlyerState
    data object TodoSphereError : TodoSphereAppsFlyerState
}

class TodoSphereApp : Application() {


    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@TodoSphereApp)
            modules(
                listOf(
                    todoSphereModule
                )
            )
        }
        val todoSphereAppsflyer = TodoSphereAppsflyer(this)
        val totoSphereSystemService = TotoSphereSystemService(this)
        if (totoSphereSystemService.todoSphereIsOnline()) {
            todoSphereAppsflyer.init { data ->
                todoSphereConversionFlow.value = data
            }
        }
    }

    companion object {
        var todoSphereInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val todoSphereConversionFlow: MutableStateFlow<TodoSphereAppsFlyerState> = MutableStateFlow(
            TodoSphereAppsFlyerState.TodoSphereDefault
        )
        var TODO_SPHERE_FB_LI: String? = null
        const val TODO_SPHERE_MAIN_TAG = "TodoSphereMainTag"
    }
}