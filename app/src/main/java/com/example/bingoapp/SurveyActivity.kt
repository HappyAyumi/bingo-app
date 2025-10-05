package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity

class SurveyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        val checkExercise = findViewById<CheckBox>(R.id.checkExercise)
        val checkStudy = findViewById<CheckBox>(R.id.checkStudy)
        val checkLife = findViewById<CheckBox>(R.id.checkLife)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val missions = mutableListOf<String>()

            // 各カテゴリにチェックがある場合にミッションを追加
            if (checkExercise.isChecked) {
                missions.addAll(listOf(
                    "腕立て30回", "スクワット20回", "ランニング1km", "ストレッチ10分"
                ))
            }
            if (checkStudy.isChecked) {
                missions.addAll(listOf(
                    "英単語50個暗記", "数学問題10問", "プログラミング1時間", "読書30分"
                ))
            }
            if (checkLife.isChecked) {
                missions.addAll(listOf(
                    "早起きする", "夜10時に寝る", "部屋を掃除", "水を2L飲む"
                ))
            }

            // どれも選ばれていない場合の安全対策
            if (missions.isEmpty()) {
                missions.addAll(listOf(
                    "ミッション1", "ミッション2", "ミッション3", "ミッション4",
                    "ミッション5", "ミッション6", "ミッション7", "ミッション8",
                    "ミッション9", "ミッション10", "ミッション11", "ミッション12",
                    "ミッション13", "ミッション14", "ミッション15", "ミッション16"
                ))
            }

            // 16個に満たない場合は補完
            while (missions.size < 16) {
                missions.add("ダミーミッション ${missions.size + 1}")
            }

            // MainActivity にミッションリストを渡す
            val intent = Intent(this, MainActivity::class.java)
            intent.putStringArrayListExtra("missions", ArrayList(missions.take(16)))
            startActivity(intent)
            finish()
        }
    }
}
