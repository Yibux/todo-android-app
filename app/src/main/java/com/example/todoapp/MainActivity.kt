package com.example.todoapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.database.Repository
import com.example.todoapp.database.TodoDB
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : ComponentActivity() {
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var tasksText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)
        addTaskButton = findViewById(R.id.task_fab)
        tasksText = findViewById(R.id.tasksTextView)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        taskViewModel.getTasks().observe(this) { tasks ->
            tasksText.text = tasks.toString()
        }
    }
}