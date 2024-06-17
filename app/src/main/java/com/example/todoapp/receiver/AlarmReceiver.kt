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
import com.example.todoapp.R
import com.example.todoapp.SingleTaskInfoActivity
import com.example.todoapp.model.Task
import java.time.LocalDateTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationTitle = intent.getStringExtra("task_title")
        val taskId = intent.getIntExtra("task_id", -1)
        val notificationText = "Task '${notificationTitle}' is due today."

        val contentIntent = Intent(context, SingleTaskInfoActivity::class.java).apply {
            putExtra("task_id", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = context.let {
            NotificationCompat.Builder(it, "task_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("NotificationReceiver", "Missing POST_NOTIFICATIONS permission")
                return
            }
            notify(taskId, notificationBuilder.build())
        }

        val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("alarm_$taskId")
            remove("task_name_$taskId")
            apply()
        }
    }

    companion object {
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

        fun startAlarm(
            context: Context,
            delay: Long,
            taskId : Int,
            taskTitle : String,
            taskEndDate: LocalDateTime
        ) {
            val channelId = "task_channel"
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("channelId", channelId)
                putExtra("task_id", taskId)
                putExtra("task_title", taskTitle)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerTime = (taskEndDate.toEpochSecond(java.time.ZoneOffset.UTC) * 1000) - delay
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

            val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt("alarm_$taskId", taskId)
                putString("task_name_$taskId", taskTitle)
                putString("alarm_time_$taskId", taskEndDate.toString())
                apply()
            }
        }

        private fun getAlarmFromPreferences(context: Context, taskId: Int): String? {
            val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            return sharedPreferences.getString("alarm_${taskId}_title", null)
        }

        fun cancelAlarm(context: Context, id: Int) : String {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val taskTitle = getAlarmFromPreferences(context, id)
            val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                remove("alarm_$id")
                remove("task_name_$id")
                remove("alarm_time_$id")
                apply()
            }

            alarmManager.cancel(pendingIntent)
            return taskTitle.toString()
        }

        fun rescheduleAlarm(context: Context, id: Int, interval: Long, localDateTime: LocalDateTime) {
            val taskName = cancelAlarm(context, id)
            startAlarm(context, interval, id, taskName, localDateTime)
        }
    }

}