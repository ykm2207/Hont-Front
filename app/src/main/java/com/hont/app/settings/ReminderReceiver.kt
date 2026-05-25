package com.hont.app.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hont.app.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra("notif_id", 0)
        val title = intent.getStringExtra("title") ?: "Hont"
        val message = intent.getStringExtra("message") ?: ""

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play) // TODO: 앱 전용 알림 아이콘으로 교체
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Android 13+ 알림 권한 미허용 시 무시
        }
    }
}
