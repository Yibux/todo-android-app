package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.ViewAttachmentAdapter
import com.example.todoapp.databinding.TaskInfoActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.receiver.AlarmReceiver.Companion.cancelAlarm
import com.example.todoapp.viewmodel.TaskViewModel
import java.io.File

class SingleTaskInfoActivity : ComponentActivity() {
    private lateinit var binding: TaskInfoActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private val viewAttachmentAdapter by lazy { ViewAttachmentAdapter() }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            startActivity(Intent(this@SingleTaskInfoActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(callback)

        binding = TaskInfoActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        val taskId: Int = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Log.e("SingleTaskInfoActivity", "No task ID found in intent extras")
            return
        }

        taskViewModel.getTaskById(taskId).observe(this) { task ->
            binding.titleText.text = "Title: " + task?.title
            binding.categoryText.text = "Category: " + task?.taskCategory
            binding.dateText.text = "Date: " + task?.endDate.toString().replace("T", " ")
            binding.descriptionText.text = "Description: " + task?.description
            binding.statusCheckBox.isChecked = task?.isDone ?: false
            binding.statusCheckBox.text = if (task?.isDone == true) "status (Done)" else "status (Not Done)"
            binding.statusCheckBox.isEnabled = false
            binding.notificationSwitch.isEnabled = false
            binding.notificationSwitch.isChecked = task?.notificationOn ?: false


            binding.deleteButton.setOnClickListener {
                task?.attachments?.forEach { attachmentUri ->
                    val directory = File(task.id.toString())
                    if(directory.exists()) {
                        val file = File(directory, attachmentUri)
                        val deleteSuccess = file.delete()

                        if (!deleteSuccess) {
                            Log.e("File Deletion", "Failed to delete file: $attachmentUri")
                        }
                    }

                }
                taskViewModel.deleteTask(task!!)
                cancelAlarm(this, task.id)
                val intent = Intent(this, MainActivity::class.java).apply{
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }

            binding.editButton.setOnClickListener {
                val intent = Intent(this, EditTaskActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("task_id", task?.id)
                startActivity(intent)
            }

            runOnUiThread {
                if(task?.attachments?.get(0)?.isNotEmpty() == true) {
                    viewAttachmentAdapter.differ.submitList(task.attachments)
                    binding.attachmentsRecyclerViewer.apply {
                        layoutManager = LinearLayoutManager(this@SingleTaskInfoActivity,
                            LinearLayoutManager.VERTICAL, false)
                        adapter = viewAttachmentAdapter
                    }
                } else {
                    binding.attachmentsText.text = "No attachments"
                }
            }
        }
    }
}
