package com.example.todoapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.model.Task

@Dao
interface TodoDao {
    @Query("SELECT * FROM tasks")
    fun getTodos(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id=(:id)")
    fun getTodoById(id: Int): LiveData<Task?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTodoItem(task: Task) : Long

    @Delete
    suspend fun deleteTodoItem(task: Task)

    @Update
    suspend fun updateTodoItem(task: Task)

    @Query("UPDATE tasks SET description = :attachments WHERE id = :id")
    suspend fun updateAttachments(id: Int, attachments: List<String>)
}