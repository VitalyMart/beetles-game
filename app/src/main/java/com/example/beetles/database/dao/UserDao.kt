package com.example.beetles.database.dao

import androidx.room.*
import com.example.beetles.database.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): User?
    
    @Query("SELECT * FROM users WHERE full_name = :fullName LIMIT 1")
    fun getUserByName(fullName: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Long
    
    @Update
    fun updateUser(user: User)
    
    @Delete
    fun deleteUser(user: User)
    
    @Query("DELETE FROM users")
    fun deleteAllUsers()
}