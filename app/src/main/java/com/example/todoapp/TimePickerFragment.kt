package com.example.todoapp

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    interface OnTimeSelectedListener {
        fun onTimeSelected(hour: Int, minute: Int)
    }
    private lateinit var listener: OnTimeSelectedListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        listener = activity as OnTimeSelectedListener
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        listener.onTimeSelected(hourOfDay, minute)
    }

}