package com.example.bingoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object NotificationScheduler {

    fun scheduleRandomNotification(context: Context) {
        val calendar = Calendar.getInstance()
        val hour = (8..20).random() // 朝8時〜夜8時のランダム時刻
        val minute = (0..59).random()

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // 次回通知時間を保存（重複防止）
        val prefs = context.getSharedPreferences("bingo_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("next_notification_time", calendar.timeInMillis).apply()
    }
}
