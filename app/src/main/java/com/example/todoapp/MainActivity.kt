package com.example.todoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.receiver.AlarmReceiver
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.LocalDateTime

class MainActivity : ComponentActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRecycleViewer: RecyclerView
    private lateinit var searchTask : EditText
    private val taskAdapter by lazy { TaskAdapter() }
    private var taskNumber: Int = 0
    private lateinit var handler: Handler
    private lateinit var spinner: Spinner
    private lateinit var doneTaskSwitch : Switch
    private lateinit var settings : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)
        addTaskButton = findViewById(R.id.task_fab)
        taskRecycleViewer = findViewById(R.id.taskList)
        searchTask = findViewById(R.id.searchTask)
        spinner = findViewById(R.id.allCategories)
        doneTaskSwitch = findViewById(R.id.doneTasksSwitch)
        settings = findViewById(R.id.settingsButton)
        AlarmReceiver.createNotificationChannel("task_channel", this)

        searchTask.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                taskViewModel.getTasks().value?.let { tasks ->
                    val filteredTasks = tasks.sortedWith(compareBy { it.endDate })
                        .filter { it.title.contains(s.toString(), true) }
                    taskAdapter.differ.submitList(filteredTasks)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })



        doneTaskSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked) {
                taskViewModel.getTasks().value?.let { tasks ->
                    val filteredTasks = tasks.sortedWith(compareBy { it.endDate })
                    taskAdapter.differ.submitList(filteredTasks)
                }
            } else {
                taskViewModel.getTasks().value?.let { tasks ->
                    val filteredTasks = tasks.sortedWith(compareBy { it.endDate })
                        .filter { it.isDone != isChecked }
                    taskAdapter.differ.submitList(filteredTasks)
                }
            }
        }
        //jobscheduler
        //ile przed zakonczeniem zadania dostajemy powiadomienie
        //kopiowac zalaczniki do folderu aplikacji

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        taskViewModel.getTasks().observe(this) { tasks ->
            val categories = tasks.map { it.taskCategory }.distinct().toMutableList()
            categories.add(0, "All")
            val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter
            spinner.onItemSelectedListener = this
            taskNumber = tasks.filter {
                it.endDate == LocalDateTime
                    .now() && it.notificationOn && !it.isDone
            }.size

            val tasksToSubmit = tasks.sortedBy { it.endDate }
            runOnUiThread {
                taskAdapter.differ.submitList(tasks)
                val context = this@MainActivity
                taskRecycleViewer.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = taskAdapter
                }
            }

            settings.setOnClickListener {
                val taskIds = tasks.filter { it.notificationOn && it.endDate!!.isAfter(LocalDateTime.now()) }.map { it.id }
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    putExtra("tasks_id_with_notifications", taskIds.toIntArray())
                }
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        handler = Handler(Looper.getMainLooper())
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted
        } else {
            // Permission is denied
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, postion: Int, p3: Long) {
        val category = parent?.getItemAtPosition(postion).toString()
        if(category == "All") {
            taskViewModel.getTasks().observe(this) { tasks ->
                val filteredTasks = tasks.sortedWith(compareBy { it.endDate })
                taskAdapter.differ.submitList(filteredTasks)
            }
        } else {
            taskViewModel.getTasks().observe(this) { tasks ->
                val filteredTasks = tasks.sortedWith(compareBy { it.endDate })
                    .filter { it.taskCategory == category }
                taskAdapter.differ.submitList(filteredTasks)
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}
