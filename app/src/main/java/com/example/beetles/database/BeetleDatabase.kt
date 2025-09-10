package com.example.beetles.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.beetles.database.dao.ScoreDao
import com.example.beetles.database.dao.UserDao
import com.example.beetles.database.entities.Score
import com.example.beetles.database.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Score::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BeetleDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun scoreDao(): ScoreDao
    
    companion object {
        @Volatile
        private var INSTANCE: BeetleDatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): BeetleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeetleDatabase::class.java,
                    "beetle_database"
                )
                    .addCallback(BeetleDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class BeetleDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.userDao(), database.scoreDao())
                }
            }
        }
        
        suspend fun populateDatabase(userDao: UserDao, scoreDao: ScoreDao) {
            // Можно добавить начальные данные при необходимости
            userDao.deleteAllUsers()
            scoreDao.deleteAllScores()
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}