package com.example.bingoapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnOk: Button
    private lateinit var btnRetake: Button
    private var photoPath: String? = null
    private var missionIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        imageView = findViewById(R.id.previewImage)
        btnOk = findViewById(R.id.btnOk)
        btnRetake = findViewById(R.id.btnRetake)

        photoPath = intent.getStringExtra("PHOTO_PATH")
        missionIndex = intent.getIntExtra("MISSION_INDEX", -1)

        // 画像プレビュー表示
        photoPath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            imageView.setImageBitmap(bitmap)
        }

        // OK → 保存して結果を返す
        btnOk.setOnClickListener {
            val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
            prefs.edit().putString("photo_$missionIndex", photoPath).apply()

            val resultIntent = Intent().apply {
                putExtra("MISSION_INDEX", missionIndex)
                putExtra("PHOTO_PATH", photoPath)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // 撮り直し → カメラに戻る
        btnRetake.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java).apply {
                putExtra("MISSION_INDEX", missionIndex)
            }
            startActivity(intent)
            finish()
        }
    }
}
