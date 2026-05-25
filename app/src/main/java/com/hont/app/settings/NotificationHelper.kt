package com.hont.app.settings

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hont.app.R
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID = "hont_reminders"
    const val NOTIF_ID_WORKOUT = 1001
    const val NOTIF_ID_BREAKFAST = 1002
    const val NOTIF_ID_LUNCH = 1003
    const val NOTIF_ID_DINNER = 1004

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "운동/식단 알림", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "운동 및 식단 기록 리마인더" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, notifId: Int, hour: Int, minute: Int, title: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, notifId, title, message)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            // 이미 지난 시각이면 내일로
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel(context: Context, notifId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, notifId, "", ""))
    }

    private fun buildPendingIntent(
        context: Context, notifId: Int, title: String, message: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("notif_id", notifId)
            putExtra("title", title)
            putExtra("message", message)
        }
        return PendingIntent.getBroadcast(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "오전" else "오후"
        val h = if (hour % 12 == 0) 12 else hour % 12
        return "$amPm ${h}:${minute.toString().padStart(2, '0')}"
    }
}
