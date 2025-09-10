package com.example.beetles.database.dao

import androidx.room.*
import com.example.beetles.database.entities.Score
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    
    @Query("SELECT * FROM scores ORDER BY score DESC, created_at DESC")
    fun getAllScores(): Flow<List<Score>>
    
    @Query("SELECT * FROM scores WHERE user_id = :userId ORDER BY score DESC, created_at DESC")
    fun getScoresByUserId(userId: Long): Flow<List<Score>>
    
    @Query("""
        SELECT s.*, u.full_name 
        FROM scores s 
        INNER JOIN users u ON s.user_id = u.id 
        ORDER BY s.score DESC, s.created_at DESC 
        LIMIT :limit
    """)
    fun getTopScoresWithUserNames(limit: Int = 10): Flow<List<ScoreWithUserName>>
    
    @Query("""
        SELECT s.*, u.full_name 
        FROM scores s 
        INNER JOIN users u ON s.user_id = u.id 
        WHERE s.difficulty = :difficulty 
        ORDER BY s.score DESC, s.created_at DESC 
        LIMIT :limit
    """)
    fun getTopScoresByDifficulty(difficulty: Int, limit: Int = 10): Flow<List<ScoreWithUserName>>
    
    @Insert
    fun insertScore(score: Score): Long
    
    @Update
    fun updateScore(score: Score)
    
    @Delete
    fun deleteScore(score: Score)
    
    @Query("DELETE FROM scores WHERE user_id = :userId")
    fun deleteScoresByUserId(userId: Long)
    
    @Query("DELETE FROM scores")
    fun deleteAllScores()
}

data class ScoreWithUserName(
    val id: Long,
    @ColumnInfo(name = "user_id") val userId: Long,
    val score: Int,
    val difficulty: Int,
    @ColumnInfo(name = "game_time") val gameTime: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "full_name") val fullName: String
)