package com.example.bingoapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 100
    }

    private var isFocusMode = false
    private var focusTimer: CountDownTimer? = null

    private lateinit var focusButton: Button
    private lateinit var resetButton: Button
    private lateinit var levelResetButton: Button
    private lateinit var timerText: TextView
    private lateinit var bingoGrid: GridLayout
    private lateinit var themeSpinner: Spinner

    private lateinit var levelText: TextView
    private lateinit var levelProgress: ProgressBar

    private val PREFS_NAME = "UserProgress"
    private val KEY_POINTS = "points"
    private val bingoSize = 4

    private val topicsByTheme = mapOf(
        "趣味" to listOf("流行りの漫画を読む", "新作映画鑑賞", "アクセサリーを手作りする", "自然の写真を撮る", "好きなアーティストのライブ映像を見る",
            "お菓子作りに挑戦する", "今日の日記を書く", "公園の遊具で遊ぶ", "スケッチブックに絵を描く", "花を育ててみる",
            "習字で抱負を書く", "ジグゾーパズルをする", "部屋の一か所を掃除ずる", "何かの参考書を買いに行く", "ガチャガチャをする",
            "30分散歩", "お気に入りの写真を現像する", "粘土で工作する", "カラオケで90点とる", "楽器をはじめてみる"),
        "運動" to listOf("6キロランニングする", "腕立て伏せを50回する", "ヨガのポーズをする", "スクワット20回を4セット", "サイクリングで公園に行く",
            "プールに行って泳ぐ", "30分ウォーキングする", "講演でサッカーする", "体育館でバスケする", "体育館でバレーボールする",
            "卓球をする", "ジムに行ってみる", "バドミントンする", "テニスする", "バッティングセンターに行く",
            "スポッチャに行く", "縄跳びで2重飛びを20回連続でする", "ボルダリングに挑戦する", "腕立て伏せ100回挑戦", "懸垂を10回する"),
        "勉強" to listOf("プログラミングに挑戦する", "数学に問題集を5ページ進める", "英語の問題集を5ページ進める" , "好きな歴史上の人物について調べてまとめる", "理科の問題集を5ページ進める",
            "今日の日記を書く", "興味のある資格試験のテキストを買いに行く", "資格試験のテキストを5ページ進める", "かっこいい4字熟語を見つける", "机周りを片づける",
            "いらない教科書を捨てる", "好きな画家を見つける", "英単語を新しく20個覚える", "好きな歴史上の人物の生い立ちをまとめる", "歴史博物館に行く",
            "科学博物館に行く", "スタバで勉強する", "コメダ珈琲で勉強する", "ALTの先生と英語で話す", "いらない文房具を捨てる")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        focusButton = findViewById(R.id.focusButton)
        resetButton = findViewById(R.id.resetButton)
        levelResetButton = findViewById(R.id.levelResetButton)
        timerText = findViewById(R.id.timerText)
        bingoGrid = findViewById(R.id.bingoGrid)
        themeSpinner = findViewById(R.id.themeSpinner)
        levelText = findViewById(R.id.levelText)
        levelProgress = findViewById(R.id.levelProgress)

        updateLevelUI() // レベルUI更新

        // Spinner 設定
        val themes = topicsByTheme.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedTheme = themes[position]
                displayBingoSheet(selectedTheme)
                restoreSavedSelection()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        focusButton.setOnClickListener {
            if (!isFocusMode) startFocusMode(10 * 60 * 1000)
            else stopFocusMode()
        }

        resetButton.setOnClickListener { resetBingoSheet() }

        levelResetButton.setOnClickListener {
            resetLevel()
        }

        restoreThemeAndSelection()
    }

    // --------------------
    // ポイント＆レベル
    // --------------------
    private fun addPoints(points: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val current = prefs.getInt(KEY_POINTS, 0)
        val newTotal = (current + points).coerceAtLeast(0)
        prefs.edit().putInt(KEY_POINTS, newTotal).apply()

        animatePointGain(points)
        updateLevelUI()
    }

    private fun resetLevel() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putInt(KEY_POINTS, 0).apply()
        updateLevelUI()
        Toast.makeText(this, "レベルをリセットしました", Toast.LENGTH_SHORT).show()
    }

    private fun getPoints(): Int = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_POINTS, 0)

    private fun getLevelName(points: Int): String = when {
        points < 100 -> "凡人"
        points < 400 -> "努力家"
        points < 800 -> "達人"
        points < 1000 -> "仙人"
        else -> "神"
    }

    private fun updateLevelUI() {
        val points = getPoints()
        val levelName = getLevelName(points)
        val levelProgressValue = points % 100
        levelProgress.progress = levelProgressValue
        val levelNumber = points / 100 + 1
        levelText.text = "$levelName (Lv.$levelNumber)"
    }

    private fun animatePointGain(points: Int) {
        if (points <= 0) return
        val textView = TextView(this).apply {
            text = "+${points}pt!"
            textSize = 22f
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
            alpha = 0f
            translationY = 0f
        }
        val layout = findViewById<LinearLayout>(R.id.mainLayout)
        layout.addView(textView)
        textView.animate()
            .alpha(1f)
            .translationYBy(-150f)
            .setDuration(800)
            .withEndAction { layout.removeView(textView) }
            .start()
    }

    private fun onCellCompleted() = addPoints(10)
    private fun onBingoAchieved() {
        addPoints(50)
        Toast.makeText(this, "ビンゴ！+50pt✨", Toast.LENGTH_SHORT).show()
    }

    // --------------------
    // ビンゴ盤生成
    // --------------------
    private fun generateBingoTopics(theme: String, count: Int) = (topicsByTheme[theme] ?: listOf()).shuffled().take(count)

    private fun displayBingoSheet(theme: String) {
        bingoGrid.removeAllViews()
        val topics = generateBingoTopics(theme, bingoSize * bingoSize)

        for (topic in topics) {
            val frame = FrameLayout(this)
            val textView = TextView(this).apply {
                text = topic
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(8)
                background = ContextCompat.getDrawable(context, android.R.drawable.btn_default)
                setOnClickListener {
                    toggleSelection(this)
                    onCellCompleted()
                    if (checkBingo()) onBingoAchieved()
                    saveCurrentState()
                    launchCameraForCell(bingoGrid.indexOfChild(frame))
                }
            }

            val imageView = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
            frame.addView(textView)
            frame.addView(imageView)

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(6, 6, 6, 6)
            }

            bingoGrid.addView(frame, params)

            val file = File(filesDir, "cell_${bingoGrid.indexOfChild(frame)}.jpg")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun toggleSelection(view: TextView) {
        val selectedColor = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val defaultColor = ContextCompat.getColor(this, android.R.color.transparent)
        val bg = view.background
        val currentColor = if (bg is android.graphics.drawable.ColorDrawable) bg.color else defaultColor
        view.setBackgroundColor(if (currentColor == selectedColor) defaultColor else selectedColor)
    }

    private fun checkBingo(): Boolean {
        for (i in 0 until bingoSize) {
            if ((0 until bingoSize).all { col -> isCellSelected(i * bingoSize + col) }) return true
            if ((0 until bingoSize).all { row -> isCellSelected(row * bingoSize + i) }) return true
        }
        if ((0 until bingoSize).all { i -> isCellSelected(i * bingoSize + i) }) return true
        if ((0 until bingoSize).all { i -> isCellSelected(i * bingoSize + (bingoSize - 1 - i)) }) return true
        return false
    }

    private fun isCellSelected(index: Int): Boolean {
        val frame = bingoGrid.getChildAt(index) as? FrameLayout ?: return false
        val textView = frame.getChildAt(0) as? TextView ?: return false
        val selectedColor = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val bg = textView.background
        val currentColor = if (bg is android.graphics.drawable.ColorDrawable) bg.color else 0
        return currentColor == selectedColor
    }

    // --------------------
    // 永続化
    // --------------------
    private fun saveCurrentState() {
        val prefs = getSharedPreferences("bingoPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        for (i in 0 until bingoSize * bingoSize) editor.putBoolean("cell_$i", isCellSelected(i))
        editor.putString("currentTheme", themeSpinner.selectedItem.toString())
        editor.apply()
    }

    private fun restoreThemeAndSelection() {
        val prefs = getSharedPreferences("bingoPrefs", MODE_PRIVATE)
        val theme = prefs.getString("currentTheme", null)
        val themes = topicsByTheme.keys.toList()
        if (theme != null) {
            val idx = themes.indexOf(theme)
            if (idx >= 0) themeSpinner.setSelection(idx)
        }
    }

    private fun restoreSavedSelection() {
        val prefs = getSharedPreferences("bingoPrefs", MODE_PRIVATE)
        for (i in 0 until bingoSize * bingoSize) {
            val selected = prefs.getBoolean("cell_$i", false)
            val frame = bingoGrid.getChildAt(i) as? FrameLayout ?: continue
            val textView = frame.getChildAt(0) as? TextView ?: continue
            textView.setBackgroundColor(
                if (selected) ContextCompat.getColor(this, android.R.color.holo_blue_light)
                else ContextCompat.getColor(this, android.R.color.transparent)
            )
        }
    }

    private fun resetBingoSheet() {
        for (i in 0 until bingoSize * bingoSize) {
            val frame = bingoGrid.getChildAt(i) as? FrameLayout ?: continue
            val textView = frame.getChildAt(0) as? TextView ?: continue
            textView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            val imageView = frame.getChildAt(1) as? ImageView ?: continue
            imageView.setImageBitmap(null)
            val file = File(filesDir, "cell_$i.jpg")
            if (file.exists()) file.delete()
        }
        saveCurrentState()
        Toast.makeText(this, "ビンゴシートをリセットしました", Toast.LENGTH_SHORT).show()
    }

    // --------------------
    // カメラ起動
    // --------------------
    private fun launchCameraForCell(cellIndex: Int) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("cellIndex", cellIndex)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val cellIndex = data?.getIntExtra("cellIndex", -1) ?: return
            val frame = bingoGrid.getChildAt(cellIndex) as? FrameLayout ?: return
            val imageView = frame.getChildAt(1) as? ImageView ?: return
            val file = File(filesDir, "cell_$cellIndex.jpg")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    // --------------------
    // 集中モード
    // --------------------
    private fun startFocusMode(durationMillis: Long) {
        isFocusMode = true
        focusButton.text = "集中モード終了"
        Toast.makeText(this, "集中モード開始！", Toast.LENGTH_SHORT).show()

        focusTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerText.text = "残り ${minutes}分${seconds}秒"
            }
            override fun onFinish() {
                isFocusMode = false
                focusButton.text = "集中モード開始"
                timerText.text = "集中モード終了！"
                addPoints(20) // 集中モードボーナス
                Toast.makeText(applicationContext, "お疲れ様でした！", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun stopFocusMode() {
        isFocusMode = false
        focusTimer?.cancel()
        focusButton.text = "集中モード開始"
        timerText.text = ""
        Toast.makeText(this, "集中モードを終了しました。", Toast.LENGTH_SHORT).show()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isFocusMode) {
            Toast.makeText(this, "集中モード中は他アプリに移動できません！", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        focusTimer?.cancel()
    }
}
