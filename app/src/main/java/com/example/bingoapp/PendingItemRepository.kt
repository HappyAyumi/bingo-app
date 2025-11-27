package com.example.bingoapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PendingItemRepository {

    private const val PREF_NAME = "PendingApproval"
    private const val KEY_PENDING = "pending_items"

    /** 承認待ちに追加（他のActivityから呼び出して使う） */
    fun addPending(context: Context, reason: String, points: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

        val array = JSONArray(listStr)

        val obj = JSONObject().apply {
            put("reason", reason)
            put("points", points)
        }

        array.put(obj)

        prefs.edit().putString(KEY_PENDING, array.toString()).apply()
    }

    /** 表示用に文字列リスト化 */
    fun getPendingStrings(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

        val array = JSONArray(listStr)
        val result = mutableListOf<String>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val text = "【${obj.getString("reason")}】 +${obj.getInt("points")} pt"
            result.add(text)
        }

        return result
    }

    /** 全承認 → 合計ポイントを返す */
    fun approveAll(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

        val array = JSONArray(listStr)
        var total = 0

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            total += obj.getInt("points")
        }

        // 承認後はクリア
        prefs.edit().putString(KEY_PENDING, "[]").apply()

        return total
    }
}