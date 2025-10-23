package com.example.bingoapp

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val imageView = findViewById<ImageView>(R.id.previewImage)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnRetake = findViewById<Button>(R.id.btnRetake)

        val photoPath = intent.getStringExtra("PHOTO_PATH")
        val missionIndex = intent.getIntExtra("MISSION_INDEX", -1)

        // 画像を読み込んで表示
        if (photoPath != null) {
            val bitmap = BitmapFactory.decodeFile(photoPath)
            imageView.setImageBitmap(bitmap)
        }

        btnSave.setOnClickListener {
            if (photoPath != null && missionIndex != -1) {
                val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
                prefs.edit().putString("photo_$missionIndex", photoPath).apply()

                // 達成済みに追加
                val completed = prefs.getStringSet("completed", emptySet())!!.toMutableSet()
                completed.add(missionIndex.toString())
                prefs.edit().putStringSet("completed", completed).apply()
            }
            setResult(Activity.RESULT_OK)
            finish()
        }

        btnRetake.setOnClickListener {
            // 取り直し → カメラに戻る
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
