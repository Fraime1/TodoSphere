package com.sofutil.todosw.eriger.domain.usecases

import android.util.Log
import com.sofutil.todosw.eriger.data.repo.TodoSphereRepository
import com.sofutil.todosw.eriger.data.utils.TodoSpherePushToken
import com.sofutil.todosw.eriger.data.utils.TotoSphereSystemService
import com.sofutil.todosw.eriger.domain.model.TodoSphereEntity
import com.sofutil.todosw.eriger.domain.model.TodoSphereParam
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp

class TodoSphereGetAllUseCase(
    private val todoSphereRepository: TodoSphereRepository,
    private val totoSphereSystemService: TotoSphereSystemService,
    private val todoSpherePushToken: TodoSpherePushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : TodoSphereEntity?{
        val params = TodoSphereParam(
            todoSphereLocale = totoSphereSystemService.todoSphereGetLocale(),
            todoSpherePushToken = todoSpherePushToken.todoSphereGetToken(),
            todoSphereAfId = totoSphereSystemService.todoSphereGetAppsflyerId()
        )
//        todoSphereSystemService.bubblePasswrodGetGaid()
        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Params for request: $params")
        return todoSphereRepository.todoSphereGetClient(params, conversion)
    }



}