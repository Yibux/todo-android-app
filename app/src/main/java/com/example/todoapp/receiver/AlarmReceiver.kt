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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

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
        Log.d("AlarmReceiverNotification", "${sharedPreferences.all}")
        with(sharedPreferences.edit()) {
            remove("alarm_$taskId")
            remove("task_name_$taskId")
            remove("alarm_time_$taskId")
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
            val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            val delayTimeInMinutes = sharedPreferences.getLong("notification_hour", 1)
            Log.d("AlarmReceiver", "Delay time in minutes: $delayTimeInMinutes")

            val taskEndTimeInMillis = taskEndDate.toEpochSecond(ZoneOffset.UTC) * 1000
            val currentTimeInMillis = System.currentTimeMillis()
            val delayTimeInMillis = delayTimeInMinutes * 60 * 1000
            val delay = taskEndTimeInMillis - currentTimeInMillis - delayTimeInMillis

            if (delay > 0) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent)
                val millis = System.currentTimeMillis() + delay
                val instant = Instant.ofEpochMilli(millis)
                val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                Log.d("AlarmReceiver", "Alarm set for task ID: $taskId at $taskEndDate with delay: $delay")
                Log.d("AlarmReceiver", "Hour of the alarm: $localDateTime")
            } else {
                Log.d("AlarmReceiver", "Computed delay is negative, not setting the alarm.")
            }

            with(sharedPreferences.edit()) {
                putInt("alarm_$taskId", taskId)
                apply()
            }
            with(sharedPreferences.edit()) {
                putString("task_name_$taskId", taskTitle)
                apply()
            }
            with(sharedPreferences.edit()) {
                putString("alarm_time_$taskId", taskEndDate.toString())
                apply()
            }
            Log.d("AlarmReceiver", "${sharedPreferences.all}")
        }

        private fun getAlarmFromPreferences(context: Context, taskId: Int, option: Int): String? {
            val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            return when (option) {
                0 -> {
                    sharedPreferences.getString("task_name_$taskId", null)
                }
                1 -> {
                    sharedPreferences.getString("alarm_time_$taskId", null)
                }
                else -> {
                    sharedPreferences.getString("alarm_pendingIntent_$taskId", null)
                }
            }
        }

        fun cancelAlarm(context: Context, id: Int) : Container {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val taskTitle = getAlarmFromPreferences(context, id, 0)
            val taskDueDate = getAlarmFromPreferences(context, id, 1)

            if(taskTitle == null || taskDueDate == null) {
                return Container("", LocalDateTime.now())
            }
            val taskConvertedDueDate = LocalDateTime.parse(taskDueDate)

            alarmManager.cancel(pendingIntent)
            Log.d("AlarmReceiver", "Alarm canceled for task ID: $id")

            val sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
            Log.d("AlarmReceiverCancel", "${sharedPreferences.all}")
            with(sharedPreferences.edit()) {
                remove("alarm_$id")
                apply()
            }
            with(sharedPreferences.edit()) {
                remove("task_name_$id")
                apply()
            }
            with(sharedPreferences.edit()) {
                remove("alarm_time_$id")
                apply()
            }
            return Container(taskTitle.toString(), taskConvertedDueDate)
        }

        fun rescheduleAlarm(context: Context, id: Int) {
            val container = cancelAlarm(context, id)
            startAlarm(context, id, container.taskName, container.taskTime)
        }

        fun rescheduleAlarmWithLocalDateTime(
            context: Context,
            id: Int,
            newDueDate: LocalDateTime
        ) {
            val container = cancelAlarm(context, id)
             startAlarm(context, id, container.taskName, newDueDate)
        }
    }

    class Container (
        val taskName : String,
        val taskTime : LocalDateTime
        )

}