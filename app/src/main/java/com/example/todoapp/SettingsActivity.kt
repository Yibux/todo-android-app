package com.example.todoapp

import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.todoapp.databinding.SettingsLayoutBinding

class SettingsActivity : ComponentActivity(){
    private lateinit var binding: SettingsLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = SettingsLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val sharedPreferences = getSharedPreferences("notification_hour", MODE_PRIVATE)
        val notificationHour = sharedPreferences.getString("notification_hour", "8:00")

        val options = arrayOf("5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00")

        for (i in options.indices) {
            val radioButton = RadioButton(this)

            radioButton.text = options[i]
            radioButton.id = i

            binding.notificationHour.addView(radioButton)
        }
        binding.notificationHour.check(options.indexOf(notificationHour))

        binding.notificationHour.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPreferences.edit()
            editor.putString("notification_hour", options[checkedId])
            editor.apply()


        }

    }
}
