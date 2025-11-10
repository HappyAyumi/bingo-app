package com.example.bingoapp.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bingoapp.MainActivity
import com.example.bingoapp.SurveyActivity
import java.io.File

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saveFile = File(filesDir, "bingo_data.json")

        if (saveFile.exists() && saveFile.length() > 0) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, SurveyActivity::class.java))
        }
        finish()
    }
}