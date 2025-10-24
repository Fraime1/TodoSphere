package com.sofutil.todosw.eriger.domain.model

import com.google.gson.annotations.SerializedName


data class TodoSphereEntity (
    @SerializedName("ok")
    val todoSphereOk: String,
    @SerializedName("url")
    val todoSphereUrl: String,
    @SerializedName("expires")
    val todoSphereExpires: Int,
)