package com.sofutil.todosw

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ValueCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.sofutil.todosw.eriger.TodoSphereGlobalLayoutUtil
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp
import com.sofutil.todosw.eriger.presentation.pushhandler.TodoSpherePushHandler
import com.sofutil.todosw.eriger.todoSphereSetupSystemBars
import org.koin.android.ext.android.inject

class TodoSphereActivity : AppCompatActivity() {

    lateinit var todoSpherePhoto: Uri
    var todoSphereFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    val todoSphereTakeFile = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        todoSphereFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
    }

    val todoSphereTakePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            todoSphereFilePathFromChrome?.onReceiveValue(arrayOf(todoSpherePhoto))
        } else {
            todoSphereFilePathFromChrome?.onReceiveValue(null)
        }
    }

    private val todoSpherePushHandler by inject<TodoSpherePushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todoSphereSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_todo_sphere)
        val todoSphereRootView = findViewById<View>(android.R.id.content)
        TodoSphereGlobalLayoutUtil().todoSphereAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(todoSphereRootView) { todoSphereView, todoSphereInsets ->
            val todoSphereSystemBars = todoSphereInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val todoSphereDisplayCutout = todoSphereInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val todoSphereIme = todoSphereInsets.getInsets(WindowInsetsCompat.Type.ime())


            val todoSphereTopPadding = maxOf(todoSphereSystemBars.top, todoSphereDisplayCutout.top)
            val todoSphereLeftPadding = maxOf(todoSphereSystemBars.left, todoSphereDisplayCutout.left)
            val todoSphereRightPadding = maxOf(todoSphereSystemBars.right, todoSphereDisplayCutout.right)
            window.setSoftInputMode(TodoSphereApp.todoSphereInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "ADJUST PUN")
                val todoSphereBottomInset = maxOf(todoSphereSystemBars.bottom, todoSphereDisplayCutout.bottom)

                todoSphereView.setPadding(todoSphereLeftPadding, todoSphereTopPadding, todoSphereRightPadding, 0)

                todoSphereView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = todoSphereBottomInset
                }
            } else {
                Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "ADJUST RESIZE")

                val todoSphereBottomInset = maxOf(todoSphereSystemBars.bottom, todoSphereDisplayCutout.bottom, todoSphereIme.bottom)

                todoSphereView.setPadding(todoSphereLeftPadding, todoSphereTopPadding, todoSphereRightPadding, 0)

                todoSphereView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = todoSphereBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Activity onCreate()")
        todoSpherePushHandler.todoSphereHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            todoSphereSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        todoSphereSetupSystemBars()
    }
}