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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.AttachmentAdapter
import com.example.todoapp.databinding.AddTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.receiver.AlarmReceiver
import com.example.todoapp.receiver.AlarmReceiver.Companion.startAlarm
import com.example.todoapp.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AddTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener {
    private lateinit var binding: AddTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    private var attachments: MutableList<String> = mutableListOf()
    private val attachmentAdapter by lazy { AttachmentAdapter() }

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

        binding.addTaskButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                addTask()
            }
        }

        binding.pickDate.setOnClickListener {
            val datePicker = DatePickerFragment()
            datePicker.show(supportFragmentManager, "datePicker")
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
                    if(attachments.size == 1 && attachments[0].length == 0)
                        attachments[0] = uri.toString()
                    else
                        attachments.add(uri.toString())
                }
            } else {
                val uri = result.data?.data
                val fileName = getFileNameFromUri(uri)
                if(attachments.size == 1 && attachments[0].length == 0) {
                    attachments[0] = uri.toString() + "\n" + fileName.toString()
                }
                else {
                    attachments.add(uri.toString() + "\n" + fileName.toString())
                }
            }

            runOnUiThread {
                if(attachments.size != 1 || attachments[0].length != 0) {
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

    fun getFileNameFromUri(uri: Uri?): String? {
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
                binding.dateText.text!!.isNotEmpty()
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        selectedDate = LocalDate.of(year, month + 1, day)
        binding.dateText.text = selectedDate.toString()
        activeTaskButton()
    }

    private suspend fun addTask() {
        val title = binding.titleEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val category = binding.categoryEditText.text.toString()
        val endDate = LocalDate.now()
        var newAttachments: List<String>? = null
        if(attachments.size != 1 || attachments[0].length != 0) {
            newAttachments = attachments
        }
        val notificationEnabled = binding.notificationSwitch.isChecked

        val task = Task(0,
            title,
            description,
            false,
            selectedDate,
            endDate,
            notificationEnabled,
            category,
            newAttachments
        )
        lifecycleScope.launch(Dispatchers.IO) {
            taskViewModel.addTask(task)
            if (notificationEnabled) {
                task.id = taskViewModel.getMaxId() + 1
                withContext(Dispatchers.Main) {
                    makeText(this@AddTaskActivity, "id: $task", Toast.LENGTH_SHORT).show()
                }
                AlarmReceiver.createNotification(this@AddTaskActivity, task)
            }
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}