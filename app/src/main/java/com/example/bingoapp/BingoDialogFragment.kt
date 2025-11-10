package com.example.bingoapp

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class BingoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("🎉 ビンゴ達成！")
            .setMessage("おめでとうございます！3マスが揃いました！")
            .setPositiveButton("OK", null)
            .create()
    }
}
