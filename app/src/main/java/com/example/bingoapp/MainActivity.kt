package com.example.bingoapp

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var adapter: BingoAdapter
    private var imageCapture: ImageCapture? = null
    private var currentPosition: Int = -1
    private var tempPhotoFile: File? = null

    private val missions = List(16) { "ミッション ${it + 1}" }

    private val previewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val savedUriString = result.data?.getStringExtra("savedUri")
            val savedUri = Uri.parse(savedUriString)
            adapter.setImage(currentPosition, savedUri)
            saveToGallery(File(savedUri.path!!))
        }
        closeCamera()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.CAMERA] == true
            if (!granted) {
                Log.e("Camera", "Camera permission not granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)

        adapter = BingoAdapter(missions) { position ->
            currentPosition = position
            startCamera()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = adapter

        btnCapture.setOnClickListener { takePhoto() }
        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startCamera() {
        previewView.visibility = View.VISIBLE
        btnCapture.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        tempPhotoFile = File(externalCacheDir, name)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempPhotoFile!!).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(tempPhotoFile)
                    openPreview(savedUri)
                }
            })
    }

    private fun openPreview(uri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("imageUri", uri.toString())
        previewLauncher.launch(intent)
    }

    private fun saveToGallery(photoFile: File) {
        val name = photoFile.name
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BingoApp")
            }
        }

        val resolver = contentResolver
        val uri: Uri? =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                photoFile.inputStream().use { input ->
                    outputStream?.use { output -> input.copyTo(output) }
                }
                Log.i("Gallery", "Photo saved to gallery: $it")
            } catch (e: Exception) {
                Log.e("Gallery", "Failed to save image to gallery", e)
            }
        }
    }

    private fun closeCamera() {
        previewView.visibility = View.GONE
        btnCapture.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}
