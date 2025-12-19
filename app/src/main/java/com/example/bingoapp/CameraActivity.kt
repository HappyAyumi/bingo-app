package com.example.bingoapp

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
    }

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var cellIndex: Int = -1
    private var cellTaskFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔴 カメラ権限チェック（最重要）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            return
        }

        setContentView(R.layout.activity_camera)

        cellIndex = intent.getIntExtra("cellIndex", -1)
        cellTaskFromIntent = intent.getStringExtra("cellTask")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCamera()
        }, ContextCompat.getMainExecutor(this))

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            takePhoto()
        }

        findViewById<Button>(R.id.btnSwitchCamera)?.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK

            startCamera()
        }
    }

    // =====================
    // カメラ起動
    // =====================
    private fun startCamera() {
        val provider = cameraProvider ?: return
        val previewView = findViewById<PreviewView>(R.id.previewView)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        // インカメラ時は左右反転表示
        previewView.scaleX =
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) -1f else 1f

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraActivity", "Camera start failed", e)
        }
    }

    // =====================
    // 撮影
    // =====================
    private fun takePhoto() {
        val capture = imageCapture ?: return
        if (cellIndex < 0) return

        val photoFile = File(filesDir, "cell_${cellIndex}.jpg")
        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    fixImageRotation(photoFile)

                    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                        fixMirror(photoFile)
                    }

                    saveToGallery(photoFile)

                    val mission =
                        cellTaskFromIntent ?: MissionRepository.getMission(cellIndex)

                    PendingItemRepository.addPending(
                        context = this@CameraActivity,
                        reason = mission,
                        points = 10,
                        cellIndex = cellIndex,
                        taskName = mission
                    )

                    Toast.makeText(
                        this@CameraActivity,
                        "承認待ちに追加しました",
                        Toast.LENGTH_SHORT
                    ).show()

                    val resultIntent = Intent().apply {
                        putExtra("cellIndex", cellIndex)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed", exception)
                    Toast.makeText(
                        this@CameraActivity,
                        "撮影に失敗しました",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // =====================
    // 権限結果
    // =====================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            recreate() // 許可後に再生成
        } else {
            Toast.makeText(this, "カメラ権限が必要です", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // =====================
    // 画像補正
    // =====================
    private fun fixMirror(photoFile: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val matrix = Matrix().apply { preScale(-1f, 1f) }
            val mirrored = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true
            )
            FileOutputStream(photoFile).use {
                mirrored.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: Exception) {
            Log.e("CameraActivity", "Mirror fix failed", e)
        }
    }

    private fun fixImageRotation(photoFile: File) {
        try {
            val exif = ExifInterface(photoFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val rotated = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
                else -> bitmap
            }

            FileOutputStream(photoFile).use {
                rotated.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: Exception) {
            Log.e("CameraActivity", "Rotation fix failed", e)
        }
    }

    private fun rotate(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degree) }
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
    }

    // =====================
    // ギャラリー保存
    // =====================
    private fun saveToGallery(photoFile: File) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BingoApp")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        uri?.let {
            contentResolver.openOutputStream(it)?.use { out ->
                photoFile.inputStream().copyTo(out)
            }
        }
    }
}
