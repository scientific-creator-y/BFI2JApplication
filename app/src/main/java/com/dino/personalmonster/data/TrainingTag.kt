package com.dino.personalmonster.data

enum class TrainingTag(
    val key: String,
    val displayName: String
) {
    READING("reading", "読書"),
    SCHOOL_REFUSAL("school_refusal", "不登校"),
    DIET("diet", "ダイエット"),
    MEDITATION("meditation", "瞑想"),
    ANTI_AGING("anti_aging", "アンチエンジング"),
    EDUCATION("education", "教育"),
    CREATION("creation", "創作活動"),
    ACHIEVEMENT("achievement", "目標達成"),
    STUDY("study", "勉強"),
    PERFECTIONISM("perfectionism", "完璧主義"),
    INTROVERSION("introversion", "内向的な生き方"),
    ANGER_MANAGEMENT("anger_management", "アンガーマネジメント"),
    MANAGEMENT("management", "起業・経営");

    companion object {
        fun fromKey(key: String): TrainingTag? {
            return values().find { it.key == key }
        }
    }
}
