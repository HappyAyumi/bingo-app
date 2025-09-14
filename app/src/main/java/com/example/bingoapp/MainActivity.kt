package com.example.bingoapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var bingoAdapter: BingoAdapter
    private var photoUri: Uri? = null
    private var selectedPosition: Int = -1 // どのマスをタップしたか記録

    // カメラで撮影
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null && selectedPosition >= 0) {
            bingoAdapter.setImage(selectedPosition, photoUri!!)
        }
    }

    // ギャラリーから取得
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && selectedPosition >= 0) {
            bingoAdapter.setImage(selectedPosition, uri)
        }
    }

    // 実行時パーミッションリクエスト
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            // 権限が拒否された場合の処理（必要ならトースト表示など）
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4) // ✅ 4×4 グリッド

        // ダミーデータ（16個）
        val missions = List(16) { i -> "Mission ${i + 1}" }

        // アダプタに「マスをタップしたら権限チェックしてカメラ起動」
        bingoAdapter = BingoAdapter(missions) { position ->
            selectedPosition = position
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }

        recyclerView.adapter = bingoAdapter
    }

    /** カメラ起動 */
    private fun openCamera() {
        val photoFile = File.createTempFile("bingo_photo_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        takePicture.launch(photoUri)
    }

    /** ギャラリーから選択（未使用だが呼べる） */
    private fun openGallery() {
        pickImage.launch("image/*")
    }
}
