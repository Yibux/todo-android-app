package com.example.todoapp

import android.util.Log
import com.example.todoapp.database.TodoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugDatabase(private val todoDao: TodoDao) {
    fun logAllTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = todoDao.getTodos()
            Log.d("DatabaseDebug", tasks.toString())
        }
    }
}
