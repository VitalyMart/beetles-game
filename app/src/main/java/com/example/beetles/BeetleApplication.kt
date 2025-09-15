package com.example.beetles

import android.app.Application
import com.example.beetles.database.BeetleDatabase
import com.example.beetles.database.repository.BeetleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BeetleApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob())
    
    val database by lazy { BeetleDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { BeetleRepository(database.userDao(), database.scoreDao()) }
}