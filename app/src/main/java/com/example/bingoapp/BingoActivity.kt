package com.example.bingoapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class BingoActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private val cellImageViews = mutableListOf<ImageView>()
    private var selectedCellIndex: Int = -1

    private val cameraLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUriString = result.data?.getStringExtra("capturedImageUri")
                imageUriString?.let {
                    val imageUri = Uri.parse(it)
                    if (selectedCellIndex != -1) {
                        cellImageViews[selectedCellIndex].setImageURI(imageUri)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bingo)

        gridLayout = findViewById(R.id.gridLayout)
        setupBingoGrid()
    }

    private fun setupBingoGrid() {
        val cellCount = 9 // 3x3 の例
        for (i in 0 until cellCount) {
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 300
                    height = 300
                    marginEnd = 8
                    bottomMargin = 8
                }
                setImageResource(android.R.drawable.ic_menu_gallery)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    selectedCellIndex = i
                    val intent = Intent(this@BingoActivity, CameraActivity::class.java)
                    cameraLauncher.launch(intent)
                }
            }
            gridLayout.addView(imageView)
            cellImageViews.add(imageView)
        }
    }
}
