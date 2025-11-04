package com.example.bingoapp

object MissionGenerator {

    // 各テーマごとのお題リスト
    private val studyMissions = listOf(
        "英単語を10個覚える", "本を1章読む", "計算練習を5問解く",
        "ノートを整理する", "予習を10分する", "暗記カードを作る",
        "漢字を5個覚える", "テスト範囲を確認する", "授業の復習をする",
        "英語のリスニングを5分する", "理科の実験動画を見る", "社会の地図を覚える",
        "苦手科目を1ページ進める", "次の授業の準備をする", "日記を書く",
        "今日学んだことをまとめる"
    )

    private val exerciseMissions = listOf(
        "ストレッチをする", "散歩を10分する", "腕立て伏せを10回する",
        "スクワットを20回する", "縄跳びを30回する", "ランニングを5分する",
        "体操をする", "ダンスを1曲踊る", "ヨガをする", "階段を使う",
        "姿勢を正す", "早歩きで移動する", "ジャンプを10回する", "ボール投げをする",
        "軽い筋トレをする", "深呼吸を5回する"
    )

    private val hobbyMissions = listOf(
        "好きな曲を聴く", "絵を描く", "写真を撮る", "ゲームをする",
        "動画を見る", "料理をする", "お菓子を作る", "読書をする",
        "日記をつける", "楽器を練習する", "服をコーディネートする",
        "DIYをする", "植物を眺める", "散歩しながら空を撮る",
        "友達にメッセージを送る", "好きな言葉をメモする"
    )

    private val lifeMissions = listOf(
        "部屋を片付ける", "洗い物をする", "掃除機をかける", "ごみを捨てる",
        "早起きする", "水を飲む", "早寝する", "家族に話しかける",
        "手伝いをする", "洗濯をたたむ", "カーテンを開ける",
        "冷蔵庫を整理する", "予定を立てる", "財布を整理する",
        "買い物リストを作る", "今日の良かったことを3つ書く"
    )

    // ✅ テーマごとに16個のお題をランダム生成
    fun generateMissionsForTheme(theme: String): List<String> {
        val source = when (theme) {
            "勉強" -> studyMissions
            "運動" -> exerciseMissions
            "趣味" -> hobbyMissions
            "生活" -> lifeMissions
            else -> studyMissions
        }
        return source.shuffled().take(16)
    }

    // ✅ デフォルト（テーマ未選択時）
    fun getDefaultMissions(): List<String> = studyMissions.shuffled().take(16)
}
