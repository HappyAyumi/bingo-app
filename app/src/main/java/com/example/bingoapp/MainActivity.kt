package com.example.bingoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        // ダミーデータ（16マス）
        val items = listOf(
            BingoItem(text = "読書する"),
            BingoItem(text = "ランニング"),
            BingoItem(imageResId = android.R.drawable.ic_menu_camera),
            BingoItem(text = "英単語50個"),
            BingoItem(text = "掃除する"),
            BingoItem(imageResId = android.R.drawable.ic_menu_gallery),
            BingoItem(text = "日記を書く"),
            BingoItem(text = "友達に連絡"),
            BingoItem(text = "ストレッチ"),
            BingoItem(imageResId = android.R.drawable.ic_menu_compass),
            BingoItem(text = "料理する"),
            BingoItem(text = "ゲーム30分"),
            BingoItem(text = "買い物に行く"),
            BingoItem(text = "瞑想する"),
            BingoItem(imageResId = android.R.drawable.ic_menu_call),
            BingoItem(text = "早寝する")
        )

        recyclerView.adapter = BingoAdapter(items)
    }
}
