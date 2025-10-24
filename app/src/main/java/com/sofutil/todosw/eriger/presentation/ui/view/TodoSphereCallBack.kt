package com.sofutil.todosw.eriger.presentation.ui.view


import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.ValueCallback

interface TodoSphereCallBack {
    fun todoSphereHandleCreateWebWindowRequest(todoSphereVi: TodoSphereVi)

    fun todoSphereOnPermissionRequest(todoSphereRequest: PermissionRequest?)

    fun todoSphereOnShowFileChooser(todoSphereFilePathCallback: ValueCallback<Array<Uri>>?)

    fun todoSphereOnFirstPageFinished()
}