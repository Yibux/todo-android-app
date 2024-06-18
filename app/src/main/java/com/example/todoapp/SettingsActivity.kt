package com.example.todoapp

import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.todoapp.databinding.SettingsLayoutBinding
import com.example.todoapp.receiver.AlarmReceiver.Companion.rescheduleAlarmWithLocalDateTime
import java.time.LocalDateTime

class SettingsActivity : ComponentActivity(){
    private lateinit var binding: SettingsLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = SettingsLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val taskIds = intent.getIntArrayExtra("tasks_id_with_notifications")
        val sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE)
        val notificationHour = sharedPreferences.getLong("notification_hour", 1)

        val options = arrayOf("1 minute", "3 minutes", "5 minutes", "10 minutes", "30 minutes")

        for (i in options.indices) {
            val radioButton = RadioButton(this)

            radioButton.text = options[i]
            radioButton.id = i

            binding.notificationHour.addView(radioButton)
        }
        options.forEach {
            if (it.split(" ")[0] == notificationHour.toString()) {
                binding.notificationHour.check(options.indexOf(it))
            }
        }

        binding.notificationHour.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPreferences.edit()
            editor.remove("notification_hour")
            editor.putLong("notification_hour", options[checkedId].split(" ")[0].toLong())
            editor.apply()

            for (taskId in taskIds!!) {
                val alarmTimeString = sharedPreferences.getString("alarm_time_$taskId", "")
                if (alarmTimeString != null) {
                    if(alarmTimeString.isNotEmpty()) {
                        val localDateTime = LocalDateTime.parse(alarmTimeString)
                        rescheduleAlarmWithLocalDateTime(this, taskId,
                            localDateTime)
                    }
                }
            }
        }

    }
}
