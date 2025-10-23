package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: BingoAdapter
    private lateinit var recyclerView: RecyclerView

    private val missions = listOf(
        "写真を撮る", "ジャンプする", "笑顔", "ピース",
        "友達と撮る", "食べ物", "風景", "手を上げる",
        "後ろ姿", "影", "靴", "建物",
        "空", "草", "本", "道"
    )

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val index = result.data?.getIntExtra("MISSION_INDEX", -1) ?: -1
            if (index != -1) {
                val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
                val path = prefs.getString("photo_$index", null)
                if (path != null) adapter.updatePhoto(index, path)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        adapter = BingoAdapter(this, missions) { position ->
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("MISSION_INDEX", position)
            cameraLauncher.launch(intent)
        }
        recyclerView.adapter = adapter
    }
}
