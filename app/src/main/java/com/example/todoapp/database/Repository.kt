package com.example.todoapp.database

import androidx.lifecycle.LiveData
import com.example.todoapp.model.Task

class Repository(private val todoDao: TodoDao) {

    fun getTodoDao(): TodoDao {
        return todoDao
    }

    fun getTodos(): LiveData<List<Task>> {
        return todoDao.getTodos()
    }

    fun getTodoById(id: Int): LiveData<Task?> {
        return todoDao.getTodoById(id)
    }

    suspend fun addTodoItem(task: Task) {
        todoDao.addTodoItem(task)
    }

    suspend fun deleteTodoItem(task: Task) {
        todoDao.deleteTodoItem(task)
    }

    suspend fun updateTodoItem(task: Task) {
        todoDao.updateTodoItem(task)
    }
}
