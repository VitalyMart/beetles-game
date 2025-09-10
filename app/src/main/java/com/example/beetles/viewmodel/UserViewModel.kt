package com.example.beetles.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.beetles.database.entities.Score
import com.example.beetles.database.entities.User
import com.example.beetles.database.repository.BeetleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val repository: BeetleRepository) : ViewModel() {
    
    val allUsers = repository.getAllUsers().asLiveData()
    
    fun getUserById(id: Long, callback: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserById(id)
            withContext(Dispatchers.Main) {
                callback(user)
            }
        }
    }
    
    fun getUserByName(fullName: String, callback: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByName(fullName)
            withContext(Dispatchers.Main) {
                callback(user)
            }
        }
    }
    
    fun insertUser(user: User, callback: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = repository.insertUser(user)
            withContext(Dispatchers.Main) {
                callback(userId)
            }
        }
    }
    
    fun updateUser(user: User) = viewModelScope.launch {
        repository.updateUser(user)
    }
    
    fun deleteUser(user: User) = viewModelScope.launch {
        repository.deleteUser(user)
    }
    
    fun insertScore(score: Score, callback: ((Long) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val scoreId = repository.insertScore(score)
            callback?.let {
                withContext(Dispatchers.Main) {
                    it(scoreId)
                }
            }
        }
    }
    
    fun getTopScores() = repository.getTopScores().asLiveData()
    
    fun getTopScores(callback: (List<com.example.beetles.database.dao.ScoreWithUserName>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Простой способ получить данные из Flow
            repository.getTopScores().collect { scores ->
                withContext(Dispatchers.Main) {
                    callback(scores)
                }
                return@collect // Получаем только первое значение
            }
        }
    }
    
    fun getTopScoresByDifficulty(difficulty: Int) = 
        repository.getTopScoresByDifficulty(difficulty).asLiveData()
}

class UserViewModelFactory(private val repository: BeetleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}