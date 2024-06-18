package com.example.todoapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.AddTaskActivity.Companion.calculateDelayToNotificationTime
import com.example.todoapp.AddTaskActivity.Companion.getNotificationTime
import com.example.todoapp.adapter.AttachmentAdapter
import com.example.todoapp.databinding.EditTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.receiver.AlarmReceiver
import com.example.todoapp.receiver.AlarmReceiver.Companion.rescheduleAlarmWithLocalDateTime
import com.example.todoapp.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EditTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener,
    AttachmentAdapter.OnAttachmentDeletedListener, TimePickerFragment.OnTimeSelectedListener {
    private lateinit var binding: EditTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now()
    private var attachments: MutableList<String> = mutableListOf()
    private val attachmentAdapter by lazy { AttachmentAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditTaskActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        attachmentAdapter.onAttachmentDeletedListener = this

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val taskId: Int = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Log.e("EditTaskActivity", "Invalid task ID")
            return
        }

        binding.titleEditText.addTextChangedListener(textWatcher)
        binding.descriptionEditText.addTextChangedListener(textWatcher)
        binding.categoryEditText.addTextChangedListener(textWatcher)
        binding.dateText.addTextChangedListener(textWatcher)
        activeTaskButton()

        taskViewModel.getTaskById(taskId).observe(this) { task ->
            if(task == null) {
                return@observe
            }
            binding.titleEditText.setText(task.title)
            binding.categoryEditText.setText(task.taskCategory)
            val date = task.endDate?.toLocalDate()
            val time = task.endDate?.toLocalTime()
            binding.dateText.text = date.toString()
            selectedDate = date!!
            binding.timeText.text = time.toString()
            selectedTime = time!!
            binding.descriptionEditText.setText(task.description)
            binding.notificationSwitch.isChecked = task.notificationOn
            binding.statusCheckBox.isChecked = task.isDone
            binding.statusCheckBox.text = if (task.isDone) "status (Done)" else "status (Not Done)"
            attachments = task.attachments!!.toMutableList()
            binding.editTaskButton.setOnClickListener {
                editTask(task)
            }
            updateUI()
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

        updateUI()
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
                val newFile = File(applicationContext.filesDir, fileName.toString())
                copyFile(uri!!, newFile)
                if(attachments.size == 1 && attachments[0].isEmpty()) {
                    attachments[0] = uri.toString() + "\n" + fileName.toString()
                }
                else {
                    attachments.add(uri.toString() + "\n" + fileName.toString())
                }
                updateUI()
            }
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


    private fun updateUI() {
        runOnUiThread {
            if (attachments.isNotEmpty() && (attachments.size != 1 || attachments[0].isNotEmpty())) {
                attachmentAdapter.differ.submitList(attachments)
                binding.attachmentsRecyclerViewer2.apply {
                    layoutManager = LinearLayoutManager(this@EditTaskActivity,
                        LinearLayoutManager.VERTICAL, false)
                    adapter = attachmentAdapter
                }
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            activeTaskButton()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun activeTaskButton() {
        binding.editTaskButton.isEnabled = binding.titleEditText.text!!.isNotEmpty() &&
                binding.descriptionEditText.text!!.isNotEmpty() &&
                binding.categoryEditText.text!!.isNotEmpty() &&
                binding.dateText.text!!.isNotEmpty()
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        selectedDate = LocalDate.of(year, month + 1, day)
        binding.dateText.text = selectedDate.toString()
        activeTaskButton()
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        selectedTime = LocalTime.of(hour, minute)
        binding.timeText.text = selectedTime.toString()
    }

    private fun editTask(task: Task?) {
        if (task == null) {
            return
        }
        task.title = binding.titleEditText.text.toString()
        task.taskCategory = binding.categoryEditText.text.toString()
        task.endDate = LocalDateTime.now()
        task.description = binding.descriptionEditText.text.toString()
        task.isDone = binding.statusCheckBox.isChecked
        task.notificationOn = binding.notificationSwitch.isChecked
        task.attachments = attachments
        task.endDate = LocalDateTime.of(selectedDate, selectedTime)
        val sharedProvider = getSharedPreferences("alarms", MODE_PRIVATE)
        lifecycleScope.launch(Dispatchers.IO) {
            taskViewModel.updateTask(task)
            if (binding.notificationSwitch.isChecked) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditTaskActivity, "id: $task", Toast.LENGTH_SHORT).show()
                }
                val delay = getNotificationTime(sharedProvider, selectedDate, selectedTime)
                if (delay > 0) {
                    rescheduleAlarmWithLocalDateTime(
                        this@EditTaskActivity,
                        task.id,
                        LocalDateTime.of(selectedDate, selectedTime)
                    )
                } else {
//                    makeText(, "Invalid notification time", Toast.LENGTH_SHORT).show()
                }
            } else {
                val alarmId = sharedProvider.getInt("alarm_${task.id}", -1)
                if (alarmId != -1) {
                    AlarmReceiver.cancelAlarm(this@EditTaskActivity, task.id)
                }
            }
        }

        val intent = Intent(this, SingleTaskInfoActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
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

    override fun onAttachmentDeleted(id: Int) {
        attachments.removeAt(id)
        updateUI()
    }
}
