package com.example.bingoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: BingoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnReset: Button

    private var missions = listOf<String>()

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
        btnReset = findViewById(R.id.btnReset)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        val prefs = getSharedPreferences("bingo_prefs", MODE_PRIVATE)
        val selectedTheme = prefs.getString("selected_theme", "勉強") ?: "勉強"

        // ✅ テーマに応じたお題を生成
        missions = MissionGenerator.generateMissionsForTheme(selectedTheme)

        adapter = BingoAdapter(this, missions) { position ->
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("MISSION_INDEX", position)
            cameraLauncher.launch(intent)
        }
        recyclerView.adapter = adapter

        // ✅ 「ビンゴカードを更新」ボタン処理
        btnReset.setOnClickListener {
            missions = MissionGenerator.generateMissionsForTheme(selectedTheme)
            prefs.edit().putStringSet("missions", missions.toSet()).apply()

            // 画像情報リセット
            val editor = prefs.edit()
            missions.indices.forEach { i -> editor.remove("photo_$i") }
            editor.apply()

            adapter = BingoAdapter(this, missions) { position ->
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra("MISSION_INDEX", position)
                cameraLauncher.launch(intent)
            }
            recyclerView.adapter = adapter
        }
    }
}
