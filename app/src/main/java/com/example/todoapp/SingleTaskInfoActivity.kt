package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.ViewAttachmentAdapter
import com.example.todoapp.databinding.TaskInfoActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel

class SingleTaskInfoActivity : ComponentActivity() {
    private lateinit var binding: TaskInfoActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private val viewAttachmentAdapter by lazy { ViewAttachmentAdapter() }
    private val REQUEST_CODE_OPEN_DOCUMENT = 1

    val callback = object : OnBackPressedCallback(true) { // Enabled by default
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
        val task_id: Int = intent.getIntExtra("task_id", 0)
        //convert na stringa
        val task: LiveData<Task?> = taskViewModel.getTaskById(task_id)

        taskViewModel.getTaskById(task_id).observe(this) { task ->
            binding.titleText.text = "Title: " + task?.title
            binding.categoryText.text = "Category: " + task?.taskCategory
            binding.dateText.text = "Date: " + task?.endDate.toString()
            binding.descriptionText.text = "Description: " + task?.description
            binding.statusCheckBox.isChecked = task?.isDone ?: false
            binding.statusCheckBox.text = if (task?.isDone == true) "status (Done)" else "status (Not Done)"
            binding.statusCheckBox.isEnabled = false
            binding.notificationSwitch.isEnabled = false
            binding.notificationSwitch.isChecked = task?.notificationOn ?: false


            binding.deleteButton.setOnClickListener {
                taskViewModel.deleteTask(task!!)
                val intent = Intent(this, MainActivity::class.java).apply{
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }


        runOnUiThread {
            if(task.value?.attachments?.get(0)?.isNotEmpty() == true) {
                viewAttachmentAdapter.differ.submitList(task.value?.attachments)
                binding.attachmentsRecyclerViewer.apply {
                    layoutManager = LinearLayoutManager(this@SingleTaskInfoActivity,
                        LinearLayoutManager.VERTICAL, false)
                    adapter = viewAttachmentAdapter
                }
            }
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("task", task.value?.id)
            startActivity(intent)
        }


    }
}
