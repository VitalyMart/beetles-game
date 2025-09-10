package com.example.beetles.database.repository

import com.example.beetles.database.dao.ScoreDao
import com.example.beetles.database.dao.ScoreWithUserName
import com.example.beetles.database.dao.UserDao
import com.example.beetles.database.entities.Score
import com.example.beetles.database.entities.User
import kotlinx.coroutines.flow.Flow

class BeetleRepository(private val userDao: UserDao, private val scoreDao: ScoreDao) {
    
    // User operations
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    
    fun getUserById(id: Long): User? = userDao.getUserById(id)
    
    fun getUserByName(fullName: String): User? = userDao.getUserByName(fullName)
    
    fun insertUser(user: User): Long = userDao.insertUser(user)
    
    fun updateUser(user: User) = userDao.updateUser(user)
    
    fun deleteUser(user: User) = userDao.deleteUser(user)
    
    // Score operations
    fun getAllScores(): Flow<List<Score>> = scoreDao.getAllScores()
    
    fun getScoresByUserId(userId: Long): Flow<List<Score>> = scoreDao.getScoresByUserId(userId)
    
    fun getTopScores(limit: Int = 10): Flow<List<ScoreWithUserName>> = 
        scoreDao.getTopScoresWithUserNames(limit)
    
    fun getTopScoresByDifficulty(difficulty: Int, limit: Int = 10): Flow<List<ScoreWithUserName>> = 
        scoreDao.getTopScoresByDifficulty(difficulty, limit)
    
    fun insertScore(score: Score): Long = scoreDao.insertScore(score)
    
    fun updateScore(score: Score) = scoreDao.updateScore(score)
    
    fun deleteScore(score: Score) = scoreDao.deleteScore(score)
    
    fun deleteScoresByUserId(userId: Long) = scoreDao.deleteScoresByUserId(userId)
}