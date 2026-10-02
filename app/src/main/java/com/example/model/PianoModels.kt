package com.example.model

enum class HandType(val persianLabel: String) {
    RIGHT("دست راست"),
    LEFT("دست چپ"),
    BOTH("هر دو دست")
}

enum class NoteFeedbackType(val persianLabel: String) {
    NONE(""),
    CORRECT("عالی (درست)"),
    WRONG_PITCH("نت اشتباه"),
    TOO_EARLY("کمی زود"),
    TOO_LATE("کمی دیر"),
    MISSED("جا افتاده")
}

data class PianoNote(
    val id: String,
    val midi: Int, // e.g. 60 for Middle C (C4)
    val hand: HandType = HandType.RIGHT,
    val startBeat: Float, // beat position in the song (e.g. 0.0, 1.0, 1.5)
    val durationBeats: Float = 1.0f,
    val barNumber: Int = 1
) {
    val isBlack: Boolean
        get() {
            val semitone = midi % 12
            return semitone == 1 || semitone == 3 || semitone == 6 || semitone == 8 || semitone == 10
        }

    val frequency: Float
        get() = (440.0 * Math.pow(2.0, (midi - 69.0) / 12.0)).toFloat()
}

data class Song(
    val id: String,
    val title: String,
    val persianTitle: String,
    val composer: String,
    val difficulty: String, // "مبتدی", "متوسط", "پیشرفته"
    val defaultBpm: Int,
    val category: String, // "موسیقی ایرانی", "کلاسیک", "پاپ", "فیلم و سریال", "آرام", "تکنیکی"
    val description: String,
    val whyRecommended: String,
    val skillsLearned: List<String>,
    val notes: List<PianoNote>,
    val totalBars: Int = 8,
    val userCompatibilityPercent: Int = 85
)

data class AssessmentResult(
    val overallLevel: String, // e.g. "متوسط رو به پیشرفته"
    val pitchAccuracy: Int, // e.g. 87%
    val rhythmAccuracy: Int, // e.g. 82%
    val tempoStability: Int, // e.g. 76%
    val technicalControl: Int, // e.g. 84%
    val twoHandCoordination: Int, // e.g. 80%
    val strengths: List<String>,
    val areasToImprove: List<String>,
    val teacherDiagnosis: String,
    val recommendedStartingBpm: Int = 70,
    val recommendedStartingSongId: String = "maroofi_golden_dreams",
    val requiresFollowUp: Boolean = false,
    val followUpType: String? = null,
    val evaluatedAtMillis: Long = System.currentTimeMillis()
)

data class FollowUpTest(
    val id: String,
    val title: String,
    val description: String,
    val notes: List<PianoNote>,
    val bpm: Int,
    val focusArea: String
)

data class PracticeStep(
    val stepNumber: Int,
    val title: String,
    val durationMinutes: Int,
    val description: String,
    val completed: Boolean = false,
    val targetBpm: Int = 70
)

data class TodayPracticePlan(
    val date: String,
    val totalMinutes: Int = 40,
    val steps: List<PracticeStep>,
    val motivationQuote: String
)

data class LearningPathStage(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val focusSkill: String,
    val exercisesCount: Int,
    val recommendedPiece: String,
    val isUnlocked: Boolean,
    val isCurrent: Boolean,
    val isCompleted: Boolean,
    val masteryPercent: Int
)

data class PianoBookRecommendation(
    val id: String,
    val title: String,
    val persianTitle: String,
    val author: String,
    val difficulty: String,
    val description: String,
    val whyRecommended: String,
    val targetSkills: List<String>
)

data class PracticeSessionRecord(
    val id: Long = 0,
    val songId: String,
    val songTitle: String,
    val timestamp: Long,
    val durationMinutes: Int,
    val accuracyPercent: Int,
    val rhythmPercent: Int,
    val tempoPercent: Int,
    val mistakesCount: Int,
    val problematicSection: String,
    val teacherFeedback: String,
    val recommendedNextStep: String
)

enum class SubscriptionType(val title: String, val priceToman: Long, val periodPersian: String) {
    FREE("طرح پایه (رایگان)", 0L, "همیشگی"),
    MONTHLY("اشتراک ماهانه آوان", 600000L, "یک ماهه"),
    ANNUAL("اشتراک طلایی سالانه آوان", 4500000L, "یک ساله")
}

data class PaymentTransaction(
    val transactionId: String,
    val planType: SubscriptionType,
    val amountToman: Long,
    val timestamp: Long,
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val referenceId: String
)

data class UserProfile(
    val id: String = "user_avan_1",
    val fullName: String = "نوازنده پیانو",
    val email: String = "pianist@avan.ir",
    val isPremium: Boolean = false,
    val currentLevel: String = "در حال ارزیابی",
    val hasCompletedAssessment: Boolean = false,
    val totalPracticeMinutes: Int = 0,
    val completedSongsCount: Int = 0,
    val currentBpm: Int = 70,
    val micSensitivity: Float = 0.7f,
    val inputLatencyMs: Int = 50
)

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "TEACHER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
