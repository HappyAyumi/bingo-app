package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CameraChallengeActivity : AppCompatActivity() {
    private lateinit var timerText: TextView
    private lateinit var btnFakeCamera: Button
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_challenge)

        timerText = findViewById(R.id.timerText)
        btnFakeCamera = findViewById(R.id.btnFakeCamera)

        startTimer()

        btnFakeCamera.setOnClickListener {
            countDownTimer.cancel()
            finish() // ここで一旦終了（本来は撮影完了）
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(10000, 1000) { // 10秒
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "残り時間: ${millisUntilFinished / 1000} 秒"
            }

            override fun onFinish() {
                // 時間切れ → 罰ゲーム画面へ
                val intent = Intent(this@CameraChallengeActivity, PenaltyActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) countDownTimer.cancel()
    }
}
