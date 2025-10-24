package com.sofutil.todosw.eriger

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp

class TodoSphereGlobalLayoutUtil {

    private var todoSphereMChildOfContent: View? = null
    private var todoSphereUsableHeightPrevious = 0

    fun todoSphereAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        todoSphereMChildOfContent = content.getChildAt(0)

        todoSphereMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val todoSphereUsableHeightNow = todoSphereComputeUsableHeight()
        if (todoSphereUsableHeightNow != todoSphereUsableHeightPrevious) {
            val todoSphereUsableHeightSansKeyboard = todoSphereMChildOfContent?.rootView?.height ?: 0
            val todoSphereHeightDifference = todoSphereUsableHeightSansKeyboard - todoSphereUsableHeightNow

            if (todoSphereHeightDifference > (todoSphereUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(TodoSphereApp.todoSphereInputMode)
            } else {
                activity.window.setSoftInputMode(TodoSphereApp.todoSphereInputMode)
            }
//            mChildOfContent?.requestLayout()
            todoSphereUsableHeightPrevious = todoSphereUsableHeightNow
        }
    }

    private fun todoSphereComputeUsableHeight(): Int {
        val r = Rect()
        todoSphereMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}