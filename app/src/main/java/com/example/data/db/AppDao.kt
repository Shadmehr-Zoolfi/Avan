package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String = "default_user"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String = "default_user"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<PracticeSessionEntity>>

    @Insert
    suspend fun insertPracticeSession(session: PracticeSessionEntity): Long

    @Query("SELECT * FROM song_progress ORDER BY lastPracticedAt DESC")
    fun getAllSongProgressFlow(): Flow<List<SongProgressEntity>>

    @Query("SELECT * FROM song_progress WHERE songId = :songId LIMIT 1")
    suspend fun getSongProgress(songId: String): SongProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongProgress(progress: SongProgressEntity)

    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransactionEntity)
}
