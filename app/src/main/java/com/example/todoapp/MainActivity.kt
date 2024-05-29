package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : ComponentActivity() {
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRecycleViewer : RecyclerView
    private val taskAdapter by lazy { TaskAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)
        addTaskButton = findViewById(R.id.task_fab)
        taskRecycleViewer = findViewById(R.id.taskList)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        taskViewModel.getTasks().observe(this) { tasks ->
            runOnUiThread {
                taskAdapter.differ.submitList(tasks)
                val context = this@MainActivity
                taskRecycleViewer.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = taskAdapter
                }
            }
        }
    }
}