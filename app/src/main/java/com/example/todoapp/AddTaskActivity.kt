package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.database.Repository
import com.example.todoapp.database.TodoDB
import com.example.todoapp.database.TodoDao
import com.example.todoapp.databinding.AddTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.type.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.UUID


class AddTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener {
    private lateinit var binding: AddTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = AddTaskActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        binding.addTaskButton.setOnClickListener {
            addTask()
        }

        binding.pickDate.setOnClickListener {
            val datePicker = DatePickerFragment()
            datePicker.show(supportFragmentManager, "datePicker")
        }
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        selectedDate = LocalDate.of(year, month + 1, day) // Month is 0-based, adjust accordingly
        binding.dateText.text = selectedDate.toString()
    }

    private fun addTask() {
        val title = binding.titleEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val category = binding.categoryEditText.text.toString()
        val endDate = LocalDate.now()
//        val endDate = createDateFromString(binding.endDateEditText.text.toString())
        val notificationEnabled = binding.notificationSwitch.isChecked

        val task = Task(0,
            title,
            description,
            false,
            LocalDate.now(),
            endDate,
            notificationEnabled,
            category,
            emptyList())

        taskViewModel.addTask(task)

        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}