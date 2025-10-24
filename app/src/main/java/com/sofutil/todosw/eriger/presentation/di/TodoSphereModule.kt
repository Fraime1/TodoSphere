package com.sofutil.todosw.eriger.presentation.di

import com.sofutil.todosw.eriger.data.repo.TodoSphereRepository
import com.sofutil.todosw.eriger.data.shar.TodoSphereSharedPreference
import com.sofutil.todosw.eriger.data.utils.TodoSpherePushToken
import com.sofutil.todosw.eriger.data.utils.TotoSphereSystemService
import com.sofutil.todosw.eriger.domain.usecases.TodoSphereGetAllUseCase
import com.sofutil.todosw.eriger.presentation.pushhandler.TodoSpherePushHandler
import com.sofutil.todosw.eriger.presentation.ui.load.TodoSphereLoadViewModel
import com.sofutil.todosw.eriger.presentation.ui.view.TodoSphereViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val todoSphereModule = module {
    factory {
        TodoSpherePushHandler()
    }
    single {
        TodoSphereRepository()
    }
    single {
        TodoSphereSharedPreference(get())
    }
    factory {
        TodoSpherePushToken()
    }
    factory {
        TotoSphereSystemService(get())
    }
    factory {
        TodoSphereGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        TodoSphereViFun(get())
    }
    viewModel {
        TodoSphereLoadViewModel(get(), get(), get())
    }
}