package com.sofutil.todosw.eriger.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sofutil.todosw.TodoSphereActivity
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp
import com.sofutil.todosw.eriger.presentation.ui.load.TodoSphereLoadFragment
import org.koin.android.ext.android.inject

class TodoSphereV : Fragment(){

    private lateinit var todoSpherePhoto: Uri
    private var todoSphereFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val todoSphereTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        todoSphereFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        todoSphereFilePathFromChrome = null
    }

    private val todoSphereTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            todoSphereFilePathFromChrome?.onReceiveValue(arrayOf(todoSpherePhoto))
            todoSphereFilePathFromChrome = null
        } else {
            todoSphereFilePathFromChrome?.onReceiveValue(null)
            todoSphereFilePathFromChrome = null
        }
    }

    private val todoSphereDataStore by activityViewModels<TodoSphereDataStore>()


    private val todoSphereViFun by inject<TodoSphereViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (todoSphereDataStore.todoSphereView.canGoBack()) {
                        todoSphereDataStore.todoSphereView.goBack()
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "WebView can go back")
                    } else if (todoSphereDataStore.todoSphereViList.size > 1) {
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "WebView can`t go back")
                        todoSphereDataStore.todoSphereViList.removeAt(todoSphereDataStore.todoSphereViList.lastIndex)
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "WebView list size ${todoSphereDataStore.todoSphereViList.size}")
                        todoSphereDataStore.todoSphereView.destroy()
                        val previousWebView = todoSphereDataStore.todoSphereViList.last()
                        attachWebViewToContainer(previousWebView)
                        todoSphereDataStore.todoSphereView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (todoSphereDataStore.todoIsFirstCreate) {
            todoSphereDataStore.todoIsFirstCreate = false
            todoSphereDataStore.todoSphereContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return todoSphereDataStore.todoSphereContainerView
        } else {
            return todoSphereDataStore.todoSphereContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "onViewCreated")
        if (todoSphereDataStore.todoSphereViList.isEmpty()) {
            todoSphereDataStore.todoSphereView = TodoSphereVi(requireContext(), object :
                TodoSphereCallBack {
                override fun todoSphereHandleCreateWebWindowRequest(todoSphereVi: TodoSphereVi) {
                    todoSphereDataStore.todoSphereViList.add(todoSphereVi)
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "WebView list size = ${todoSphereDataStore.todoSphereViList.size}")
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "CreateWebWindowRequest")
                    todoSphereDataStore.todoSphereView = todoSphereVi
                    todoSphereVi.setFileChooserHandler { callback ->
                        handleFileChooser(callback)
                    }
                    attachWebViewToContainer(todoSphereVi)
                }

                override fun todoSphereOnPermissionRequest(todoSphereRequest: PermissionRequest?) {
                    todoSphereRequest?.grant(todoSphereRequest.resources)
                }

                override fun todoSphereOnFirstPageFinished() {
                    todoSphereDataStore.todoSphereSetIsFirstFinishPage()
                }

            }, todoSphereWindow = requireActivity().window).apply {
                setFileChooserHandler { callback ->
                    handleFileChooser(callback)
                }
            }
            todoSphereDataStore.todoSphereView.todoSphereFLoad(arguments?.getString(TodoSphereLoadFragment.TODO_SPHERE_D) ?: "")
//            ejvview.fLoad("www.google.com")
            todoSphereDataStore.todoSphereViList.add(todoSphereDataStore.todoSphereView)
            attachWebViewToContainer(todoSphereDataStore.todoSphereView)
        } else {
            todoSphereDataStore.todoSphereViList.forEach { webView ->
                webView.setFileChooserHandler { callback ->
                    handleFileChooser(callback)
                }
            }
            todoSphereDataStore.todoSphereView = todoSphereDataStore.todoSphereViList.last()

            attachWebViewToContainer(todoSphereDataStore.todoSphereView)
        }
        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "WebView list size = ${todoSphereDataStore.todoSphereViList.size}")
    }

    private fun handleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        todoSphereFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Launching file picker")
                    todoSphereTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Launching camera")
                    todoSpherePhoto = todoSphereViFun.todoSphereSavePhoto()
                    todoSphereTakePhoto.launch(todoSpherePhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                todoSphereFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun attachWebViewToContainer(w: TodoSphereVi) {
        todoSphereDataStore.todoSphereContainerView.post {
            // Убираем предыдущую WebView, если есть
            (w.parent as? ViewGroup)?.removeView(w)
            todoSphereDataStore.todoSphereContainerView.removeAllViews()
            todoSphereDataStore.todoSphereContainerView.addView(w)
        }
    }


}