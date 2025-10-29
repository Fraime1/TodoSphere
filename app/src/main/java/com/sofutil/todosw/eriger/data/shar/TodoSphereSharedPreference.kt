package com.sofutil.todosw.eriger.data.shar

import android.content.Context
import androidx.core.content.edit

class TodoSphereSharedPreference(context: Context) {
    private val todoSpherePrefs = context.getSharedPreferences("todoSphereSharedPrefsAb", Context.MODE_PRIVATE)

    var todoSphereSavedUrl: String
        get() = todoSpherePrefs.getString(TODO_SPHERE_SAVED_URL, "") ?: ""
        set(value) = todoSpherePrefs.edit { putString(TODO_SPHERE_SAVED_URL, value) }

    var todoSphereExpired : Long
        get() = todoSpherePrefs.getLong(TODO_SPHERE_EXPIRED, 0L)
        set(value) = todoSpherePrefs.edit { putLong(TODO_SPHERE_EXPIRED, value) }

    var todoSphereAppState: Int
        get() = todoSpherePrefs.getInt(TODO_SPHERE_APPLICATION_STATE, 0)
        set(value) = todoSpherePrefs.edit { putInt(TODO_SPHERE_APPLICATION_STATE, value) }

    var todoSphereNotificationRequest: Long
        get() = todoSpherePrefs.getLong(TODO_SPHERE_NOTIFICAITON_REQUEST, 0L)
        set(value) = todoSpherePrefs.edit { putLong(TODO_SPHERE_NOTIFICAITON_REQUEST, value) }

    var todoSphereNotificationRequestedBefore: Boolean
        get() = todoSpherePrefs.getBoolean(TODO_SPHERE_NOTIFICATION_REQUEST_BEFORE, false)
        set(value) = todoSpherePrefs.edit { putBoolean(
            TODO_SPHERE_NOTIFICATION_REQUEST_BEFORE, value) }

    companion object {
        private const val TODO_SPHERE_SAVED_URL = "todoSphereSavedUrl"
        private const val TODO_SPHERE_EXPIRED = "todoSphereExpired"
        private const val TODO_SPHERE_APPLICATION_STATE = "todoSphereApplicationState"
        private const val TODO_SPHERE_NOTIFICAITON_REQUEST = "todoSphereNotificationRequest"
        private const val TODO_SPHERE_NOTIFICATION_REQUEST_BEFORE = "todoSphereNotificationRequestedBefore"
    }
}