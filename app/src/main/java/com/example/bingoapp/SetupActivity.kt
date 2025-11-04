package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val checkBoxContainer = findViewById<LinearLayout>(R.id.checkBoxContainer)
        val btnCreateBingo = findViewById<Button>(R.id.btnCreateBingo)

        // ✅ カテゴリと項目（例）
        val goals = mapOf(
            "勉強" to listOf("英単語を覚える", "予習をする", "レポートを早めに出す"),
            "運動" to listOf("10分走る", "ストレッチをする", "階段を使う"),
            "生活" to listOf("早寝する", "部屋を片付ける", "朝ごはんを食べる"),
            "趣味" to listOf("写真を撮る", "音楽を聴く", "本を読む")
        )

        // ✅ チェックボックスを動的に生成
        val checkBoxes = mutableListOf<CheckBox>()
        for ((category, items) in goals) {
            val categoryTitle = TextView(this).apply {
                text = "📌 $category"
                textSize = 18f
                setPadding(0, 16, 0, 8)
            }
            checkBoxContainer.addView(categoryTitle)

            for (goal in items) {
                val cb = CheckBox(this).apply { text = goal }
                checkBoxes.add(cb)
                checkBoxContainer.addView(cb)
            }
        }

        // ✅ ビンゴカード生成ボタン押下時
        btnCreateBingo.setOnClickListener {
            val selectedGoals = checkBoxes.filter { it.isChecked }.map { it.text.toString() }

            if (selectedGoals.isEmpty()) {
                Toast.makeText(this, "少なくとも1つ選択してください！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 選択内容を SharedPreferences に保存
            val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
            prefs.edit().putStringSet("selected_goals", selectedGoals.toSet()).apply()

            // ✅ MainActivity に遷移
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
