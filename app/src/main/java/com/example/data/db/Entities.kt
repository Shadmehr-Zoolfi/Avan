package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "default_user",
    val fullName: String = "نوازنده آوان",
    val email: String = "pianist@avan.ir",
    val isPremium: Boolean = false,
    val currentLevel: String = "در حال ارزیابی",
    val hasCompletedAssessment: Boolean = false,
    val pitchAccuracy: Int = 0,
    val rhythmAccuracy: Int = 0,
    val tempoStability: Int = 0,
    val technicalControl: Int = 0,
    val twoHandCoordination: Int = 0,
    val totalPracticeMinutes: Int = 0,
    val completedSongsCount: Int = 0,
    val currentBpm: Int = 70,
    val assessmentDate: Long = 0L,
    val assessmentTeacherNote: String = ""
)

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "song_progress")
data class SongProgressEntity(
    @PrimaryKey val songId: String,
    val songTitle: String,
    val masteryPercent: Int = 0,
    val highestAccuracy: Int = 0,
    val timesPracticed: Int = 0,
    val lastPracticedAt: Long = 0L,
    val isCompleted: Boolean = false
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val transactionId: String,
    val planType: String,
    val amountToman: Long,
    val timestamp: Long,
    val status: String,
    val referenceId: String
)
