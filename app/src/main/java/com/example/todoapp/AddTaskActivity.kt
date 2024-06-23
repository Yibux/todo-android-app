package com.example.todoapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.AttachmentAdapter
import com.example.todoapp.databinding.AddTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.receiver.AlarmReceiver.Companion.startAlarm
import com.example.todoapp.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AddTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener, AttachmentAdapter.OnAttachmentDeletedListener {
    private lateinit var binding: AddTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now()
    private var attachments: MutableList<String> = mutableListOf()
    private val attachmentAdapter by lazy { AttachmentAdapter() }
    private var taskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = AddTaskActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        binding.addTaskButton.isEnabled = false
        binding.titleEditText.addTextChangedListener(textWatcher)
        binding.descriptionEditText.addTextChangedListener(textWatcher)
        binding.categoryEditText.addTextChangedListener(textWatcher)
        binding.dateText.addTextChangedListener(textWatcher)

        taskId = intent.getIntExtra("task_id", -1)

        binding.addTaskButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                addTask()
            }
        }

        binding.pickDate.setOnClickListener {
            val datePicker = DatePickerFragment()
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.pickTime.setOnClickListener {
            val timePicker = TimePickerFragment()
            timePicker.show(supportFragmentManager, "timePicker")
        }

        binding.attachFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            getContent.launch(intent)
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val clipData = result.data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    if(attachments.size == 1 && attachments[0].isEmpty())
                        attachments[0] = uri.toString()
                    else
                        attachments.add(uri.toString())
                }
            } else {
                val uri = result.data?.data
                val fileName = getFileNameFromUri(uri)
                val taskDirectory = File(applicationContext.filesDir, taskId.toString())
                if(!taskDirectory.exists()) {
                    taskDirectory.mkdirs()
                    Log.d("Directory", "Generating directory $taskId")
                }
                val newFile = File(taskDirectory, fileName.toString())
                copyFile(uri!!, newFile)

                val newFileUri = Uri.fromFile(newFile)
                if(attachments.size == 1 && attachments[0].isEmpty()) {
                    attachments[0] = newFileUri.toString() + "\n" + fileName.toString()
                }
                else {
                    attachments.add(newFileUri.toString() + "\n" + fileName.toString())
                }
            }

            runOnUiThread {
                if(attachments.size != 1 || attachments[0].isNotEmpty()) {
                    attachmentAdapter.differ.submitList(attachments)
                    binding.attachmentsRecyclerViewer.apply {
                        layoutManager = LinearLayoutManager(this@AddTaskActivity,
                            LinearLayoutManager.VERTICAL, false)
                        adapter = attachmentAdapter
                    }
                }
            }
        }
    }

    private fun copyFile(uri: Uri, newFile: File) {
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(newFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileNameFromUri(uri: Uri?): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri!!, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(displayNameIndex != -1)
                    fileName = it.getString(displayNameIndex)
            }
        }
        return fileName
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            activeTaskButton()
        }

        override fun afterTextChanged(s: Editable?) {}
    }


    private fun activeTaskButton() {
        binding.addTaskButton.isEnabled = binding.titleEditText.text!!.isNotEmpty() &&
                binding.descriptionEditText.text!!.isNotEmpty() &&
                binding.categoryEditText.text!!.isNotEmpty() &&
                binding.dateText.text!!.isNotEmpty() &&
                binding.timeText.text!!.isNotEmpty()
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        selectedDate = LocalDate.of(year, month + 1, day)
        binding.dateText.text = selectedDate.toString()
        activeTaskButton()
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        selectedTime = LocalTime.of(hour, minute)
        binding.timeText.text = selectedTime.toString()
        activeTaskButton()
    }

    private suspend fun addTask() {
        val title = binding.titleEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val category = binding.categoryEditText.text.toString()
        var newAttachments: List<String>? = null
        if(attachments.size != 1 || attachments[0].isNotEmpty()) {
            newAttachments = attachments
        }
        val notificationEnabled = binding.notificationSwitch.isChecked

        val task = Task(0,
            title,
            description,
            false,
            selectedDate,
            LocalDateTime.of(selectedDate, selectedTime),
            notificationEnabled,
            category,
            newAttachments
        )

        taskViewModel.addTask(task)

        if (notificationEnabled) {
//            withContext(Dispatchers.Main) {
//                task.id = taskViewModel.getMaxId()
//            }
            startAlarm(this@AddTaskActivity, taskId, task.title,
                LocalDateTime.of(selectedDate, selectedTime))
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {

        fun getNotificationTime(
            sharedProvider: SharedPreferences,
            selectedDate: LocalDate,
            selectedTime: LocalTime
        ): Long {
            val notificationTime = sharedProvider.getString("notification_time", "1")?.toLong() ?: 1
            return calculateDelayToNotificationTime(notificationTime, selectedDate, selectedTime)
        }
        fun calculateDelayToNotificationTime(
            notificationTime: Long,
            selectedDate: LocalDate,
            selectedTime: LocalTime
        ): Long {
            val zoneId = ZoneId.systemDefault()
            val selectedDateTime = ZonedDateTime.of(selectedDate, selectedTime, zoneId)
            val beforeDelay = selectedDateTime.minusMinutes(notificationTime)
            val currentDateTime = ZonedDateTime.now(zoneId)

            val duration = Duration.between(currentDateTime, beforeDelay)
            return duration.toMillis()
        }
    }

    override fun onAttachmentDeleted(id: Int) {
        attachments.removeAt(id)
    }
}