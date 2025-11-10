package com.example.bingoapp

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PhotoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        val imageView: ImageView = findViewById(R.id.photoView)
        val path = intent.getStringExtra("photoPath")
        if (path != null) {
            Glide.with(this).load(Uri.parse(path)).into(imageView)
        }
    }
}
