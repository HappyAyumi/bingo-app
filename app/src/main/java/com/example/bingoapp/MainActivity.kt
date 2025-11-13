package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // --- 集中モード関連 ---
    private var isFocusMode = false
    private var focusTimer: CountDownTimer? = null
    private lateinit var focusButton: Button
    private lateinit var timerText: TextView

    // --- ビンゴ関連 ---
    private lateinit var bingoRecyclerView: RecyclerView
    private lateinit var bingoAdapter: BingoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLabel: TextView

    private val bingoCells = mutableListOf<BingoCell>()
    private val bingoSize = 5 // 5x5ビンゴ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- View初期化 ---
        focusButton = findViewById(R.id.focusButton)
        timerText = findViewById(R.id.timerText)
        bingoRecyclerView = findViewById(R.id.bingoRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        progressLabel = findViewById(R.id.progressLabel)

        // --- ビンゴ盤セットアップ ---
        setupBingoBoard()
        bingoAdapter = BingoAdapter(bingoCells) {
            updateProgress()
        }
        bingoRecyclerView.layoutManager = GridLayoutManager(this, bingoSize)
        bingoRecyclerView.adapter = bingoAdapter

        // --- 初期達成率 ---
        updateProgress()

        // --- 集中モードボタン ---
        focusButton.setOnClickListener {
            if (!isFocusMode) {
                startFocusMode(10 * 60 * 1000) // 例: 10分
            } else {
                stopFocusMode()
            }
        }
    }

    // --------------------------
    // 🎯 集中モード制御
    // --------------------------

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

    private fun stopFocusMode() {
        isFocusMode = false
        focusTimer?.cancel()
        focusButton.text = "集中モード開始"
        timerText.text = ""
        Toast.makeText(this, "集中モードを終了しました。", Toast.LENGTH_SHORT).show()
    }

    /** 他アプリに移動しようとしたときの制御 */
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

    // --------------------------
    // 🎯 ビンゴ機能
    // --------------------------

    private fun setupBingoBoard() {
        bingoCells.clear()
        for (i in 1..(bingoSize * bingoSize)) {
            bingoCells.add(BingoCell("マス$i", null, false))
        }
    }

    private fun updateProgress() {
        val total = bingoCells.size
        val opened = bingoCells.count { it.isOpened }
        val progress = (opened.toFloat() / total * 100).toInt()
        progressBar.progress = progress
        progressLabel.text = "達成率：$progress%"
    }
}
