package com.example.bingoapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import android.content.ContentValues
import android.provider.MediaStore

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private var cellIndex: Int = -1
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cellIndex = intent.getIntExtra("cellIndex", -1)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCamera()
        }, ContextCompat.getMainExecutor(this))

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            takePhoto()
        }

        findViewById<Button>(R.id.btnSwitchCamera)?.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT
            else
                CameraSelector.LENS_FACING_BACK
            startCamera()
        }
    }

    private fun startCamera() {
        val provider = cameraProvider ?: return
        val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(windowManager.defaultDisplay.rotation)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        previewView.scaleX = if (lensFacing == CameraSelector.LENS_FACING_FRONT) -1f else 1f

        provider.unbindAll()
        provider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(filesDir, "cell_${cellIndex}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    fixImageRotation(photoFile)

                    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                        fixMirror(photoFile)
                    }

                    saveToGallery(photoFile)

                    // ★ 承認待ちに追加
                    PendingItemRepository.addPending(
                        context = this@CameraActivity,
                        reason = "セル${cellIndex + 1} の写真撮影",
                        points = 10
                    )

                    Toast.makeText(this@CameraActivity, "承認待ちに追加しました", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra("cellIndex", cellIndex)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exception.message}", exception)
                }
            })
    }

    private fun fixMirror(photoFile: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val mirrored = BitmapFactory.decodeFile(photoFile.absolutePath)?.let {
                BitmapFactory.decodeFile(photoFile.absolutePath)
            }
            FileOutputStream(photoFile).use { out ->
                mirrored?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: Exception) {
            Log.e("CameraActivity", "Mirror fix failed: ${e.message}", e)
        }
    }

    private fun fixImageRotation(photoFile: File) {
        try {
            val exif = ExifInterface(photoFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            FileOutputStream(photoFile).use { out ->
                rotatedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
            }

        } catch (e: Exception) {
            Log.e("CameraActivity", "Failed to fix rotation: ${e.message}", e)
        }
    }

    private fun rotateBitmap(bitmap: android.graphics.Bitmap, degree: Float): android.graphics.Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveToGallery(photoFile: File) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BingoApp")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            contentResolver.openOutputStream(it).use { out ->
                photoFile.inputStream().copyTo(out!!)
            }
        }
    }
}