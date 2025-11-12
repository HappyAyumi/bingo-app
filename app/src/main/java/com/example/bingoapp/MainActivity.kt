package com.example.bingoapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BingoAdapter
    private lateinit var missions: List<String>
    private var currentPosition: Int = -1

    /** カメラ結果受け取り **/
    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoUri = result.data?.getStringExtra("photoUri") ?: return@registerForActivityResult
                val uri = Uri.parse(photoUri)
                adapter.setImage(currentPosition, uri)
                Toast.makeText(this, "写真を追加しました！", Toast.LENGTH_SHORT).show()
                checkBingo()
            } else {
                Toast.makeText(this, "写真撮影がキャンセルされました", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        missions = List(16) { i -> "Mission ${i + 1}" }

        adapter = BingoAdapter(missions) { position ->
            currentPosition = position
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    100
                )
            }
        }

        recyclerView.adapter = adapter
    }

    /** CameraActivityを起動 **/
    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        cameraResultLauncher.launch(intent)
    }

    /** ビンゴ判定 **/
    private fun checkBingo() {
        val imageStates = (0 until missions.size).map { adapter.hasImage(it) }
        val gridSize = 4

        for (i in 0 until gridSize) {
            if ((0 until gridSize).all { imageStates[i * gridSize + it] }) {
                Toast.makeText(this, "🎉 ビンゴ！横一列！", Toast.LENGTH_SHORT).show()
                return
            }
            if ((0 until gridSize).all { imageStates[it * gridSize + i] }) {
                Toast.makeText(this, "🎉 ビンゴ！縦一列！", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if ((0 until gridSize).all { imageStates[it * gridSize + it] }) {
            Toast.makeText(this, "🎉 ビンゴ！斜め！", Toast.LENGTH_SHORT).show()
            return
        }

        if ((0 until gridSize).all { imageStates[it * gridSize + (gridSize - 1 - it)] }) {
            Toast.makeText(this, "🎉 ビンゴ！斜め！", Toast.LENGTH_SHORT).show()
        }
    }
}
