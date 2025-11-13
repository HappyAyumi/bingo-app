package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var isFocusMode = false
    private var focusTimer: CountDownTimer? = null
    private lateinit var focusButton: Button
    private lateinit var timerText: TextView
    private lateinit var bingoGrid: GridLayout

    // 5×5 のビンゴ設定
    private val gridSize = 5
    private val selected = Array(gridSize) { BooleanArray(gridSize) } // 選択状態

    // 🔹 ビンゴ用お題リスト（25以上）
    private val topics = listOf(
        "朝ごはん", "友達", "勉強", "スマホ", "音楽",
        "運動", "読書", "買い物", "映画", "ゲーム",
        "休日", "旅行", "家族", "料理", "授業",
        "天気", "部活", "先生", "宿題", "学校",
        "散歩", "スポーツ", "趣味", "寝坊", "アルバイト",
        "SNS", "テスト", "図書館", "掃除", "恋愛"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        focusButton = findViewById(R.id.focusButton)
        timerText = findViewById(R.id.timerText)
        bingoGrid = findViewById(R.id.bingoGrid)

        // 集中モード
        focusButton.setOnClickListener {
            if (!isFocusMode) startFocusMode(10 * 60 * 1000)
            else stopFocusMode()
        }

        // ビンゴ生成
        displayBingoSheet()
    }

    /** 🎲 お題生成 */
    private fun generateBingoTopics(count: Int): List<String> {
        return topics.shuffled().take(count)
    }

    /** 🟩 ビンゴシート表示 */
    private fun displayBingoSheet() {
        bingoGrid.removeAllViews()
        val bingoTopics = generateBingoTopics(gridSize * gridSize)

        // 画面幅に応じて1マスのサイズを正方形にする
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val cellSize = screenWidth / gridSize - 20 // 余白分マイナス

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val index = i * gridSize + j
                val textView = TextView(this).apply {
                    text = bingoTopics[index]
                    textSize = 14f
                    gravity = android.view.Gravity.CENTER
                    background = ContextCompat.getDrawable(context, android.R.drawable.btn_default)
                    setPadding(8, 8, 8, 8)
                    width = cellSize
                    height = cellSize
                    setOnClickListener {
                        toggleSelection(this, i, j)
                    }
                }

                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(4, 4, 4, 4)
                }

                bingoGrid.addView(textView, params)
            }
        }
    }

    /** 🟦 マスの選択切り替えとビンゴ判定 */
    private fun toggleSelection(view: TextView, row: Int, col: Int) {
        val selectedColor = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val defaultColor = ContextCompat.getColor(this, android.R.color.transparent)

        if (selected[row][col]) {
            selected[row][col] = false
            view.setBackgroundColor(defaultColor)
        } else {
            selected[row][col] = true
            view.setBackgroundColor(selectedColor)
            checkBingo()
        }
    }

    /** 🎯 ビンゴ成立判定 */
    private fun checkBingo() {
        // 横方向
        for (i in 0 until gridSize) {
            if ((0 until gridSize).all { selected[i][it] }) {
                showBingoToast()
                return
            }
        }

        // 縦方向
        for (j in 0 until gridSize) {
            if ((0 until gridSize).all { selected[it][j] }) {
                showBingoToast()
                return
            }
        }

        // 斜め（左上→右下）
        if ((0 until gridSize).all { selected[it][it] }) {
            showBingoToast()
            return
        }

        // 斜め（右上→左下）
        if ((0 until gridSize).all { selected[it][gridSize - 1 - it] }) {
            showBingoToast()
            return
        }
    }

    /** 🎉 Toast表示 */
    private fun showBingoToast() {
        Toast.makeText(this, "🎉 ビンゴ達成！ 🎉", Toast.LENGTH_SHORT).show()
    }

    /** ⏱ 集中モード開始 */
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

    /** ⏹ 集中モード終了 */
    private fun stopFocusMode() {
        isFocusMode = false
        focusTimer?.cancel()
        focusButton.text = "集中モード開始"
        timerText.text = ""
        Toast.makeText(this, "集中モードを終了しました。", Toast.LENGTH_SHORT).show()
    }

    /** 🚫 集中モード中の離脱防止 */
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
