package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity

class ThemeSelectActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private var selectedTheme: String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_select)

        startButton = findViewById(R.id.startButton)

        val theme1: RadioButton = findViewById(R.id.theme1)
        val theme2: RadioButton = findViewById(R.id.theme2)
        val theme3: RadioButton = findViewById(R.id.theme3)

        theme1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTheme = "theme1"
        }
        theme2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTheme = "theme2"
        }
        theme3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTheme = "theme3"
        }

        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("selectedTheme", selectedTheme)
            startActivity(intent)
        }
    }
}
