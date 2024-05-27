package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.Repository
import com.example.todoapp.database.TodoDB
import com.example.todoapp.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(applicaiton: Application): AndroidViewModel(applicaiton) {
    private var readAllData: LiveData<List<Task>>
    private var repository: Repository

    init {
        val todoDao = TodoDB.getDatabase(applicaiton).todoDao()
        repository = Repository(todoDao)
        readAllData = repository.getTodos()
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTodoItem(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodoItem(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodoItem(task)
        }
    }

    fun getTasks(): LiveData<List<Task>> {
        return readAllData
    }
}