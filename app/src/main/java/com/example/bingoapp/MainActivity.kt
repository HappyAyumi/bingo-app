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
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    // どのラインが既にビンゴ済みかを記録するセット
    private val bingoLines = mutableSetOf<String>()
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

    // セルの選択状態を保持する配列（bingoSize が確定しているのでここで初期化可能）
    private val cellSelected = MutableList(bingoSize * bingoSize) { false }

    // 現在シートに割り当てられているお題リスト（index がセル番号と対応）
    private var currentTopics: List<String> = List(bingoSize * bingoSize) { "" }

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
        val openPendingButton = findViewById<Button>(R.id.openPendingButton)
        openPendingButton.setOnClickListener {
            val intent = Intent(this, PendingApprovalActivity::class.java)
            startActivity(intent)
        }

        focusButton = findViewById(R.id.focusButton)
        resetButton = findViewById(R.id.resetButton)
        levelResetButton = findViewById(R.id.levelResetButton)
        timerText = findViewById(R.id.timerText)
        bingoGrid = findViewById(R.id.bingoGrid)
        themeSpinner = findViewById(R.id.themeSpinner)
        levelText = findViewById(R.id.levelText)
        levelProgress = findViewById(R.id.levelProgress)
        resetCountText = findViewById(R.id.resetCountText)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        resetCount = prefs.getInt(KEY_RESET_COUNT, 0)
        updateResetCountUI()
        updateLevelUI() // レベルUI更新

        // Spinner 設定
        val themes = topicsByTheme.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedTheme = themes[position]
                // MissionRepository のテーマも更新（整合のため）
                MissionRepository.currentTheme = selectedTheme
                displayBingoSheet(selectedTheme)
                // シート生成後に保存状態を反映（ファイル画像の読み込み等を優先）
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

    // --- 承認待ちポイント保存（堅牢化） ---
    private fun addPendingPoints(
        reason: String,
        points: Int,
        cellIndex: Int,
        taskName: String
    ) {
        val prefs = getSharedPreferences("approval", MODE_PRIVATE)
        val listStr = prefs.getString("pendingList", "[]") ?: "[]"

        val array = try {
            JSONArray(listStr)
        } catch (e: Exception) {
            JSONArray()
        }

        val obj = JSONObject().apply {
            put("reason", reason)
            put("points", points)
            put("cellIndex", cellIndex)   // ★ 追加
            put("taskName", taskName)     // ★ 追加（表示用）
        }

        array.put(obj)
        prefs.edit().putString("pendingList", array.toString()).apply()
    }

    //レベルもビンゴシートの枚数も同時リセット
    private fun resetLevel() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putInt(KEY_POINTS, 0).apply()   // ポイントをリセット
        resetCount = 0                               // リセット回数を0に
        saveResetCount()                             // SharedPreferences に保存
        updateResetCountUI()                         // UI更新
        updateLevelUI()                              // レベルUI更新
        Toast.makeText(this, "レベルとリセット回数をリセットしました", Toast.LENGTH_SHORT).show()
    }

    private fun getPoints(): Int = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_POINTS, 0)

    private fun getLevelName(points: Int): String = when {
        points < 100 -> "赤子"
        points < 200 -> "オヤジ"
        points < 300 -> "兵士"
        points < 400 -> "勇者"
        points < 500 -> "金太郎"
        points < 600 -> "桃太郎"
        points < 700 -> "孫悟空"
        points < 800 -> "サムライ"
        points < 900 -> "忍者"
        points < 1000 -> "新選組"
        points < 1100 -> "社長"
        points < 1200 -> "ビリケン"
        else -> "鬼"
    }

    private fun updateLevelUI() {
        val points = getPoints()
        val levelName = getLevelName(points)
        val levelProgressValue = points % 100
        val levelNumber = points / 100 + 1

        levelProgress.progress = levelProgressValue
        levelText.text = "$levelName (Lv.$levelNumber)"

        // キャラクター画像を変更（リソース名はプロジェクトに合わせてください）
        val characterImage = findViewById<ImageView>(R.id.characterImage)
        val drawableRes = when {
            points < 100 -> R.drawable.angel
            points < 200 -> R.drawable.sakenomi_oyaji
            points < 300 -> R.drawable.military
            points < 400 -> R.drawable.yusya
            points < 500 -> R.drawable.kintaro
            points < 600 -> R.drawable.momotaro
            points < 700 -> R.drawable.son_gokuh
            points < 800 -> R.drawable.ronin_samurai
            points < 900 -> R.drawable.ninja
            points < 1000 -> R.drawable.shinsengumi_taishi
            points < 1100 -> R.drawable.shacyoh
            points < 1200 -> R.drawable.biriken
            else -> R.drawable.oni
        }
        // ImageView が存在する前提
        characterImage.setImageResource(drawableRes)
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

    // セルを選択（フラグと背景色を同期）
    private fun toggleSelection(view: TextView, index: Int) {
        cellSelected[index] = !cellSelected[index]
        view.setBackgroundColor(
            if (cellSelected[index]) ContextCompat.getColor(this, android.R.color.holo_blue_light)
            else ContextCompat.getColor(this, android.R.color.transparent)
        )
    }

    private fun onBingoAchieved(count: Int) {
        val add = 50 * count
        addPendingPoints(
            reason = "ビンゴ達成（${count}ライン）",
            points = add,
            cellIndex = -1,
            taskName = "ビンゴ"
        )
    }

    // --------------------
    // ビンゴ盤生成
    // --------------------
    private fun generateBingoTopics(theme: String, count: Int) = (topicsByTheme[theme] ?: listOf()).shuffled().take(count)
    private fun displayBingoSheet(theme: String) {
        bingoGrid.removeAllViews()
        val topics = generateBingoTopics(theme, bingoSize * bingoSize)
        currentTopics = topics

        for (topic in topics) {
            // 現在の子数をインデックスとして利用（これが新しいセルの index になる）
            val cellIndex = bingoGrid.childCount
            val frame = FrameLayout(this)
            val textView = TextView(this).apply {
                text = topic
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(8)
                background = ContextCompat.getDrawable(context, android.R.drawable.btn_default)
                // クリック時は index を渡す
                setOnClickListener {
                    toggleSelection(this, cellIndex)
                    // 選択になった場合のみ得点を追加
                    val newlyBingo = checkBingo()
                    if (newlyBingo > 0) onBingoAchieved(newlyBingo)
                    saveCurrentState()
                    launchCameraForCell(cellIndex)
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

            // 画像ファイルがあれば読み込む
            val file = File(filesDir, "cell_$cellIndex.jpg")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            }

            // セルの背景は cellSelected の状態に合わせて初期化
            textView.setBackgroundColor(
                if (cellSelected[cellIndex]) ContextCompat.getColor(this, android.R.color.holo_blue_light)
                else ContextCompat.getColor(this, android.R.color.transparent)
            )
        }
    }

    // --------------------
    // ビンゴ判定（新しく達成したライン数を返す）
    // --------------------
    private fun checkBingo(): Int {
        var newBingoCount = 0
        // 横
        for (row in 0 until bingoSize) {
            val key = "row$row"
            val isBingo = (0 until bingoSize).all { col ->
                isCellSelected(row * bingoSize + col)
            }
            if (isBingo && !bingoLines.contains(key)) {
                bingoLines.add(key)
                newBingoCount++
            }
        }
        // 縦
        for (col in 0 until bingoSize) {
            val key = "col$col"
            val isBingo = (0 until bingoSize).all { row ->
                isCellSelected(row * bingoSize + col)
            }
            if (isBingo && !bingoLines.contains(key)) {
                bingoLines.add(key)
                newBingoCount++
            }
        }
        // 左上→右下
        val diag1Key = "diag1"
        val diag1 = (0 until bingoSize).all { i ->
            isCellSelected(i * bingoSize + i)
        }
        if (diag1 && !bingoLines.contains(diag1Key)) {
            bingoLines.add(diag1Key)
            newBingoCount++
        }
        // 右上→左下
        val diag2Key = "diag2"
        val diag2 = (0 until bingoSize).all { i ->
            isCellSelected(i * bingoSize + (bingoSize - 1 - i))
        }
        if (diag2 && !bingoLines.contains(diag2Key)) {
            bingoLines.add(diag2Key)
            newBingoCount++
        }
        return newBingoCount
    }

    private fun isCellSelected(index: Int): Boolean {
        // bounds チェックで安全に
        if (index < 0 || index >= cellSelected.size) return false
        return cellSelected[index]
    }

    // --------------------
    // 永続化
    // --------------------
    private fun saveCurrentState() {
        val prefs = getSharedPreferences("bingoPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        for (i in 0 until bingoSize * bingoSize) editor.putBoolean("cell_$i", cellSelected[i])
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
        // 保存されたフラグを読み出して内部配列にセット、UIを更新
        for (i in 0 until bingoSize * bingoSize) {
            val selected = prefs.getBoolean("cell_$i", false)
            cellSelected[i] = selected

            val frame = bingoGrid.getChildAt(i) as? FrameLayout ?: continue
            val textView = frame.getChildAt(0) as? TextView ?: continue
            textView.setBackgroundColor(
                if (selected) ContextCompat.getColor(this, android.R.color.holo_blue_light)
                else ContextCompat.getColor(this, android.R.color.transparent)
            )
        }
    }

    private var resetCount: Int = 0
    private val KEY_RESET_COUNT = "resetCount"
    private lateinit var resetCountText: TextView

    private fun saveResetCount() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putInt(KEY_RESET_COUNT, resetCount).apply()
    }

    private fun updateResetCountUI() {
        resetCountText.text = "ビンゴシート: $resetCount 枚目"
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
            cellSelected[i] = false
        }
        bingoLines.clear()

        resetCount++
        saveResetCount()
        updateResetCountUI()

        saveCurrentState()
        Toast.makeText(this, "ビンゴシートをリセットしました", Toast.LENGTH_SHORT).show()
    }

    // --------------------
    // カメラ起動
    // --------------------
    private fun launchCameraForCell(cellIndex: Int) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("cellIndex", cellIndex)

        //セルに対応するお題名を追加で渡す
        val taskName = if (cellIndex in currentTopics.indices) currentTopics[cellIndex] else "お題"
        intent.putExtra("cellTask", taskName)
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
                addPendingPoints(
                    reason = "集中モード",
                    points = 20,
                    cellIndex = -1,
                    taskName = "集中モード"
                )
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