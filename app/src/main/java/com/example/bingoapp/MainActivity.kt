package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isFocusMode = false
    private var focusTimer: CountDownTimer? = null
    private lateinit var focusButton: Button
    private lateinit var timerText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 集中モード用のボタンとタイマー表示をレイアウトに追加しておく
        focusButton = findViewById(R.id.focusButton)
        timerText = findViewById(R.id.timerText)

        // 集中モード開始ボタン
        focusButton.setOnClickListener {
            if (!isFocusMode) {
                startFocusMode(10 * 60 * 1000) // 例: 10分間集中モード
            } else {
                stopFocusMode()
            }
        }
    }

    /** 集中モード開始 */
    private fun startFocusMode(durationMillis: Long) {
        isFocusMode = true
        focusButton.text = "集中モード終了"
        Toast.makeText(this, "集中モード開始！", Toast.LENGTH_SHORT).show()

        focusTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerText.text = "残り ${minutes}分${seconds}秒"
            }

            override fun onFinish() {
                isFocusMode = false
                focusButton.text = "集中モード開始"
                timerText.text = "集中モード終了！"
                Toast.makeText(applicationContext, "お疲れ様でした！", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    /** 集中モード終了 */
    private fun stopFocusMode() {
        isFocusMode = false
        focusTimer?.cancel()
        focusButton.text = "集中モード開始"
        timerText.text = ""
        Toast.makeText(this, "集中モードを終了しました。", Toast.LENGTH_SHORT).show()
    }

    /** アプリがバックグラウンドに行った時の制御 */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isFocusMode) {
            Toast.makeText(this, "集中モード中は他アプリに移動できません！", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        focusTimer?.cancel()
    }
}
