package com.example.todoapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.receiver.AlarmReceiver
import com.example.todoapp.receiver.AlarmReceiver.Companion.startAlarm
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRecycleViewer: RecyclerView
    private lateinit var searchTask : EditText
    private lateinit var timeLeftText: TextView
    private val taskAdapter by lazy { TaskAdapter() }
    private var taskNumber: Int = 0
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)
        addTaskButton = findViewById(R.id.task_fab)
        taskRecycleViewer = findViewById(R.id.taskList)
        searchTask = findViewById(R.id.searchTask)
        timeLeftText = findViewById(R.id.time_left_text)
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
        //jobscheduler
        //ile przed zakonczeniem zadania dostajemy powiadomienie
        //przycisk w powiadominiuu po nacisnieciu przenosi do aplikacji (osobny przycisk)
        // i przenosim y
        // się do zadania konkretnego
        //kopiowac zalaczniki do folderu aplikacji

        // Notification
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
            taskNumber = tasks.filter {
                it.endDate == LocalDate.now() && it.notificationOn && !it.isDone
            }.size

            val tasksToSubmit = tasks.sortedWith(compareBy { it.endDate})
            runOnUiThread {
                taskAdapter.differ.submitList(tasksToSubmit)
                val context = this@MainActivity
                taskRecycleViewer.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = taskAdapter
                }
            }
        }

//        lifecycleScope.launch {
//            val maxId = taskViewModel.getMaxId()
//            withContext(Dispatchers.Main) {
//                timeLeftText.text = "max id: $maxId"
//                print(maxId)
//            }
//        }

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
}
