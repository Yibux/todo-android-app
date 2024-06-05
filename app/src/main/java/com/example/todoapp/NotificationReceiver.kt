package com.example.todoapp

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

//class NotificationReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val notificationText = "You have tasks to do today."
//
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(
//            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notificationBuilder = NotificationCompat.Builder(context, "your_channel_id")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("Tasks")
//            .setContentText(notificationText)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//
//        with(NotificationManagerCompat.from(context)) {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            notify(1, notificationBuilder.build())
//        }
//    }
//}

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notificationText = "You have tasks to do today."

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 0, Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "your_channel_id")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Tasks")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
            notify(1, notificationBuilder.build())
        }

        return Result.success()
    }
}