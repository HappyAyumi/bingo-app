package com.example.bingoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var confirmImage: ImageView
    private lateinit var btnConfirmOk: Button
    private lateinit var btnConfirmRetry: Button
    private lateinit var previewContainer: View

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var currentPhotoPath: String? = null
    private var missionIndex: Int = -1

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "カメラの許可が必要です", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        confirmImage = findViewById(R.id.confirmImage)
        btnConfirmOk = findViewById(R.id.btnConfirmOk)
        btnConfirmRetry = findViewById(R.id.btnConfirmRetry)
        previewContainer = findViewById(R.id.previewContainer)

        missionIndex = intent.getIntExtra("MISSION_INDEX", -1)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnCapture.setOnClickListener {
            takePhoto()
        }

        btnConfirmOk.setOnClickListener {
            savePhotoAndReturn()
        }

        btnConfirmRetry.setOnClickListener {
            retryCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraActivity", "カメラ起動失敗: ", exc)
                Toast.makeText(this, "カメラの起動に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "Bingo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(System.currentTimeMillis())}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "撮影に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    currentPhotoPath = photoFile.absolutePath
                    runOnUiThread {
                        showPreview(photoFile)
                    }
                }
            }
        )
    }

    private fun showPreview(photoFile: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            confirmImage.setImageBitmap(bitmap)
            previewContainer.visibility = View.VISIBLE
            btnCapture.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("CameraActivity", "プレビュー表示失敗: ", e)
            Toast.makeText(this, "プレビューを表示できませんでした", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retryCamera() {
        previewContainer.visibility = View.GONE
        btnCapture.visibility = View.VISIBLE
        startCamera()
    }

    private fun savePhotoAndReturn() {
        val path = currentPhotoPath ?: return
        val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
        prefs.edit().putString("photo_$missionIndex", path).apply()

        Toast.makeText(this, "写真を保存しました", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
