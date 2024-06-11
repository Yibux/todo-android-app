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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskRecycleViewer: RecyclerView
    private lateinit var searchTask : EditText
    private val taskAdapter by lazy { TaskAdapter() }
    private var taskNumber: Int = 0
    private lateinit var handler: Handler
    private val interval = 15 * 60 * 1000L // 15 minutes in milliseconds
    private lateinit var timeLeftText: TextView
    private var timeLeft = interval

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)
        addTaskButton = findViewById(R.id.task_fab)
        taskRecycleViewer = findViewById(R.id.taskList)
        timeLeftText = findViewById(R.id.time_left_text)
        searchTask = findViewById(R.id.searchTask)

        // Search task by title
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
        val channelID = "task_id"
        createNotificationChannel(channelID)

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

        handler = Handler(Looper.getMainLooper())
        startRepeatingNotification(channelID)
        startCountdown()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startRepeatingNotification("task_id")
        } else {
            // Notify the user that the permission is required to show notifications
        }
    }

    private fun createAndShowNotification(taskCount: Int, channelId: String) {
        val notificationText = "You have $taskCount task to do today."
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Tasks")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = 1
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
            notify(notificationId, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(channelID: String) {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun startRepeatingNotification(channelId: String) {
        handler.post(object : Runnable {
            override fun run() {
                createAndShowNotification(taskNumber, channelId)
                handler.postDelayed(this, interval)
            }
        })
    }

    private fun startCountdown() {
        handler.post(object : Runnable {
            override fun run() {
                timeLeft -= 1000
                timeLeftText.text = "Time left: ${timeLeft / 1000 / 60}m ${(timeLeft / 1000) % 60}s"

                if (timeLeft <= 0) {
                    timeLeft = interval
                }

                handler.postDelayed(this, 1000)
            }
        })
    }
}
