package com.example.bingoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BingoAdapter

    private val missions = listOf(
        "写真を撮る", "ジャンプする", "笑顔", "ピース",
        "友達と撮る", "食べ物", "風景", "手を上げる",
        "後ろ姿", "影", "靴", "建物",
        "空", "草", "本", "道"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = BingoAdapter(this, missions)
        recyclerView.adapter = adapter

        // ✅ 起動時にシート更新チェック
        checkAndResetBingoIfNeeded()

        // ✅ 通知ボタン（テスト用）
        val btnNotify = findViewById<Button>(R.id.btnNotify)
        btnNotify.setOnClickListener {
            scheduleNotification(this)
        }

        refreshBingoBoard()
    }

    // ✅ 2週間ごとにリセット
    private fun checkAndResetBingoIfNeeded() {
        val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
        val lastReset = prefs.getLong("last_reset_time", 0L)
        val currentTime = System.currentTimeMillis()

        val daysPassed = TimeUnit.MILLISECONDS.toDays(currentTime - lastReset)

        if (daysPassed >= 14 || lastReset == 0L) {
            // 14日経過または初回起動時 → シートリセット
            prefs.edit().clear().apply()
            prefs.edit().putLong("last_reset_time", currentTime).apply()
        }
    }

    private fun scheduleNotification(context: Context) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + 3000 // 3秒後（テスト用）
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun loadCompletedMissions(): Set<Int> {
        val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
        return prefs.getStringSet("completed", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()
    }

    private fun refreshBingoBoard() {
        val completed = loadCompletedMissions()
        recyclerView.post {
            for (i in completed) {
                val holder = recyclerView.findViewHolderForAdapterPosition(i)
                holder?.itemView?.alpha = 0.4f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshBingoBoard()
    }
}
