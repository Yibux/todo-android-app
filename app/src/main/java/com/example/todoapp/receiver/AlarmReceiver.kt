package com.example.todoapp.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.IntentCompat
import com.example.todoapp.R
import com.example.todoapp.SingleTaskInfoActivity
import com.example.todoapp.model.Task
import java.util.jar.Manifest

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = intent.getStringExtra("channelId") ?: return // Early exit if channelId is null
        val task = intent.let { IntentCompat.getParcelableExtra(it, "task", Task::class.java) } ?: return // Early exit if task is null

        val notificationText = "Task '${task.title}' is due today."

        val contentIntent = Intent(context, SingleTaskInfoActivity::class.java).apply {
            putExtra("TASK_ID", task.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id, // Use task ID as unique request code
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = context.let {
            NotificationCompat.Builder(it, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set content intent
                .setAutoCancel(true)
        } ?: return // Early exit if context is null

        val notificationId = task.id // Use task ID as notification ID

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Handle permission denial more gracefully (e.g., show a dialog)
                // For now, just log a message
                Log.w("NotificationReceiver", "Missing POST_NOTIFICATIONS permission")
                return
            }
            notify(notificationId, notificationBuilder.build())
        }
    }
    companion object {
        fun startAlarm(
            context: Context,
            interval: Long,
            channelId: String,
            task: Task
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("channelId", channelId)
                putExtra("task", task)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, task.id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val triggerTime = System.currentTimeMillis() + interval
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                interval,
                pendingIntent
            )
        }

        fun cancelAlarm(context: Context, task: Task) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, task.id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }

        fun createNotificationChannel(channelID: String, context: Context) {
            val name = "Notification Channel"
            val descriptionText = "Notification channel for task reminders."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}