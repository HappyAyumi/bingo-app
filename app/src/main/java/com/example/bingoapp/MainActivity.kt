package com.example.bingoapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var bingoCells: MutableList<BingoCell>
    private lateinit var adapter: BingoAdapter
    private val CAMERA_REQUEST_CODE = 1001
    private var selectedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bingoCells = MutableList(25) { BingoCell("マス${it + 1}") }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        adapter = BingoAdapter(bingoCells) { position ->
            selectedPosition = position
            startActivityForResult(
                Intent(this, CameraActivity::class.java),
                CAMERA_REQUEST_CODE
            )
        }
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null && selectedPosition != -1) {
                bingoCells[selectedPosition].imageUri = uri
                adapter.notifyItemChanged(selectedPosition)
            }
        }
    }
}
