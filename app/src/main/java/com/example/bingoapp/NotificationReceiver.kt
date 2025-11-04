package com.example.bingoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val builder = NotificationCompat.Builder(context, "bingo_channel")
            .setSmallIcon(R.mipmap.ic_launcher) // ← 修正版
            .setContentTitle("今日のチャレンジ！")
            .setContentText("今の瞬間を撮ってビンゴを進めよう📸")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
