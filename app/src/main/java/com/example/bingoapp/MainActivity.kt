package com.example.bingoapp

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import java.io.File
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BingoAdapter

    private var selectedCell = -1
    private var photoUri: Uri? = null

    // ギャラリー選択
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null && selectedCell != -1) {
                adapter.setImage(selectedCell, uri)
            }
        }

    // カメラ撮影
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success && photoUri != null && selectedCell != -1) {
                adapter.setImage(selectedCell, photoUri!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val missions = List(16) { "ミッション${it + 1}" } // ダミーデータ

        adapter = BingoAdapter(missions) { position ->
            selectedCell = position
            showImageSourceDialog() // カメラ or ギャラリー選択ダイアログ
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4) // 4列
        recyclerView.adapter = adapter
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("カメラで撮影", "ギャラリーから選択")

        AlertDialog.Builder(this)
            .setTitle("写真を追加")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> imagePicker.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("bingo_photo_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        takePicture.launch(photoUri)
    }
}

