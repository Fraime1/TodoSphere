package com.sofutil.todosw.eriger.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sofutil.todosw.R
import com.sofutil.todosw.TodoSphereActivity

private const val TODO_SPHERE_CHANNEL_ID = "todo_sphere_notifications"

class TodoSpherePushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                todoSphereShowNotification(it.title ?: "TodoSphere", it.body ?: "", data = remoteMessage.data["url"])
            } else {
                todoSphereShowNotification(it.title ?: "TodoSphere", it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            todoSphereHandleDataPayload(remoteMessage.data)
        }
    }

    private fun todoSphereShowNotification(title: String, message: String, data: String?) {
        val todoSphereNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TODO_SPHERE_CHANNEL_ID,
                "EggSafe Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            todoSphereNotificationManager.createNotificationChannel(channel)
        }

        val todoSphereIntent = Intent(this, TodoSphereActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val todoSpherePendingIntent = PendingIntent.getActivity(
            this,
            0,
            todoSphereIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val todoSphereNotification = NotificationCompat.Builder(this, TODO_SPHERE_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.todo_sphere_noti)
            .setAutoCancel(true)
            .setContentIntent(todoSpherePendingIntent)
            .build()

        todoSphereNotificationManager.notify(System.currentTimeMillis().toInt(), todoSphereNotification)
    }

    private fun todoSphereHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}