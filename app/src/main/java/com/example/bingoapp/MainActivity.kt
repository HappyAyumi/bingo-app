package com.example.bingoapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BingoAdapter
    private lateinit var missions: List<String>
    private var currentPhotoUri: Uri? = null
    private var currentPosition: Int = -1

    /** カメラ結果受け取り **/
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && currentPhotoUri != null) {
                adapter.setImage(currentPosition, currentPhotoUri!!)
                Toast.makeText(this, "写真を追加しました！", Toast.LENGTH_SHORT).show()
                checkBingo()
            } else {
                Toast.makeText(this, "写真の撮影がキャンセルされました", Toast.LENGTH_SHORT).show()
            }
        }

    /** 権限リクエスト **/
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        // ミッション（16個）
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

    /** カメラを起動 **/
    private fun openCamera() {
        try {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )

            currentPhotoUri = photoURI
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // ✅ URI権限を全カメラアプリに付与（AQUOS対策）
            val resInfoList = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(
                    packageName,
                    photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                takePictureLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(this, "カメラアプリが見つかりませんでした", Toast.LENGTH_LONG).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "カメラを起動できませんでした: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** 一時ファイル作成 **/
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("BINGO_${timeStamp}_", ".jpg", storageDir)
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

    /** 権限リクエスト結果 **/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "カメラの許可が必要です", Toast.LENGTH_SHORT).show()
        }
    }
}
