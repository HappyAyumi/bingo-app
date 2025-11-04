package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity

class ThemeSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_select)

        val btnStart: Button = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            val selectedTheme = when {
                findViewById<RadioButton>(R.id.radioStudy).isChecked -> "勉強"
                findViewById<RadioButton>(R.id.radioExercise).isChecked -> "運動"
                findViewById<RadioButton>(R.id.radioHobby).isChecked -> "趣味"
                findViewById<RadioButton>(R.id.radioLife).isChecked -> "生活"
                else -> "勉強"
            }

            val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
            prefs.edit().putString("selected_theme", selectedTheme).apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
