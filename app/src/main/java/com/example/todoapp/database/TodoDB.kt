package com.example.todoapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todoapp.model.Task
@TypeConverters(Converters::class)
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TodoDB : RoomDatabase() {

    abstract fun todoDao(): TodoDao
    companion object {
        @Volatile
        private var INSTANCE: TodoDB? = null

        fun getDatabase(context: Context): TodoDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDB::class.java,
                    "Todo_DB"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
