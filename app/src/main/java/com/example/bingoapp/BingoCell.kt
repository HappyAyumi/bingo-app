package com.example.bingoapp

import android.net.Uri

data class BingoCell(
    val text: String,
    val imageUri: Uri? = null,
    var isOpened: Boolean = false  // ← 追加
)
