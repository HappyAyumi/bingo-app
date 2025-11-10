package com.example.bingoapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PhotoPreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnRetake: Button
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)

        imageView = findViewById(R.id.imageViewPreview)
        btnRetake = findViewById(R.id.btnRetake)
        btnClose = findViewById(R.id.btnClose)

        val photoPath = intent.getStringExtra("PHOTO_PATH")
        val index = intent.getIntExtra("MISSION_INDEX", -1)

        if (photoPath != null) {
            Glide.with(this).load(photoPath).into(imageView)
        }

        btnRetake.setOnClickListener {
            // ✅ 再撮影モードに戻る
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("MISSION_INDEX", index)
            setResult(Activity.RESULT_OK, intent)
            startActivity(intent)
            finish()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}
