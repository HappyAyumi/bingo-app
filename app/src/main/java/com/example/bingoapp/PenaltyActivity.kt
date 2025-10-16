package com.example.bingoapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PenaltyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_penalty)

        val text = findViewById<TextView>(R.id.penaltyText)
        text.text = "⏰ 時間切れ！罰ゲーム発動！"
    }
}
