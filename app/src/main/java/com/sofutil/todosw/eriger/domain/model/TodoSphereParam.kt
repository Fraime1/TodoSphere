package com.sofutil.todosw.eriger.domain.model

import com.google.gson.annotations.SerializedName


private const val TODO_SPHERE_A = "com.sofutil.todosw"
data class TodoSphereParam (
    @SerializedName("af_id")
    val todoSphereAfId: String,
    @SerializedName("bundle_id")
    val todoSphereBundleId: String = TODO_SPHERE_A,
    @SerializedName("os")
    val todoSphereOs: String = "Android",
    @SerializedName("store_id")
    val todoSphereStoreId: String = TODO_SPHERE_A,
    @SerializedName("locale")
    val todoSphereLocale: String,
    @SerializedName("push_token")
    val todoSpherePushToken: String,
    @SerializedName("firebase_project_id")
    val todoSphereFirebaseProjectId: String = "todosphere-aef7b",

    )