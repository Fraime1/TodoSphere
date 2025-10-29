package com.sofutil.todosw.eriger.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sofutil.todosw.TodoSphereActivity
import com.sofutil.todosw.eriger.presentation.ui.load.TodoSphereLoadFragment
import org.koin.android.ext.android.inject

class TodoSphereV : Fragment(){

    private val todoSphereDataStore by activityViewModels<TodoSphereDataStore>()
    lateinit var todoSphereRequestFromChrome: PermissionRequest


    private val todoSphereViFun by inject<TodoSphereViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (todoSphereDataStore.todoSphereView.canGoBack()) {
                        todoSphereDataStore.todoSphereView.goBack()
                    } else if (todoSphereDataStore.todoSphereViList.size > 1) {
                        todoSphereDataStore.todoSphereViList.removeAt(todoSphereDataStore.todoSphereViList.lastIndex)
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
        
        if (todoSphereDataStore.todoSphereViList.isEmpty()) {
            todoSphereDataStore.todoSphereView = TodoSphereVi(requireContext(), object :
                TodoSphereCallBack {
                override fun todoSphereHandleCreateWebWindowRequest(todoSphereVi: TodoSphereVi) {
                    todoSphereDataStore.todoSphereViList.add(todoSphereVi)
                    todoSphereDataStore.todoSphereView = todoSphereVi
                    attachWebViewToContainer(todoSphereVi)
                }

                override fun todoSphereOnPermissionRequest(todoSphereRequest: PermissionRequest?) {
                    if (todoSphereRequest != null) {
                        todoSphereRequestFromChrome = todoSphereRequest
                    }
                    todoSphereRequestFromChrome.grant(todoSphereRequestFromChrome.resources)
                }

                override fun todoSphereOnShowFileChooser(todoSphereFilePathCallback: ValueCallback<Array<Uri>>?) {
                    (requireActivity() as TodoSphereActivity).todoSphereFilePathFromChrome = todoSphereFilePathCallback
                    val listItems: Array<out String> =
                        arrayOf("Select from file", "To make a photo")
                    val listener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            0 -> {
                                (requireActivity() as TodoSphereActivity).todoSphereTakeFile.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            1 -> {
                                (requireActivity() as TodoSphereActivity).todoSpherePhoto = todoSphereViFun.todoSphereSavePhoto()
                                (requireActivity() as TodoSphereActivity).todoSphereTakePhoto.launch((requireActivity() as TodoSphereActivity).todoSpherePhoto)
                            }
                        }
                    }
                    AlertDialog.Builder(requireActivity())
                        .setTitle("Choose a method")
                        .setItems(listItems, listener)
                        .setCancelable(true)
                        .setOnCancelListener {
                            todoSphereFilePathCallback?.onReceiveValue(arrayOf(Uri.EMPTY))
                        }
                        .create()
                        .show()
                }

                override fun todoSphereOnFirstPageFinished() {
                    todoSphereDataStore.todoSphereSetIsFirstFinishPage()
                }

            }, todoSphereWindow = requireActivity().window)
            todoSphereDataStore.todoSphereView.todoSphereFLoad(arguments?.getString(TodoSphereLoadFragment.TODO_SPHERE_D) ?: "")
//            ejvview.fLoad("www.google.com")
            todoSphereDataStore.todoSphereViList.add(todoSphereDataStore.todoSphereView)
            attachWebViewToContainer(todoSphereDataStore.todoSphereView)
        } else {
            todoSphereDataStore.todoSphereView = todoSphereDataStore.todoSphereViList.last()
            attachWebViewToContainer(todoSphereDataStore.todoSphereView)
        }
    }

    private fun attachWebViewToContainer(w: TodoSphereVi) {
        todoSphereDataStore.todoSphereContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            todoSphereDataStore.todoSphereContainerView.removeAllViews()
            todoSphereDataStore.todoSphereContainerView.addView(w)
        }
    }


}