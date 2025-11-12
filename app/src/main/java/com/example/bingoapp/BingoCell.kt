package com.example.bingoapp

import android.net.Uri

data class BingoCell(
    val text: String,
    var imageUri: Uri? = null
)
