package com.sofutil.todosw.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTask : Screen("add_task")
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: Int) = "edit_task/$taskId"
    }
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Int) = "task_detail/$taskId"
    }
    object Statistics : Screen("statistics")
    object Profile : Screen("profile")
    object About : Screen("about")
}

