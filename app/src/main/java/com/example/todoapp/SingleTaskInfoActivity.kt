package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.AttachmentAdapter
import com.example.todoapp.databinding.TaskInfoActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel

class SingleTaskInfoActivity : ComponentActivity() {
    private lateinit var binding: TaskInfoActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private val attachmentAdapter by lazy { AttachmentAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = TaskInfoActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val task: Task? = intent.getParcelableExtra("task", Task::class.java)

        binding.titleText.text = "Title: " + task?.title
        binding.categoryText.text = "Category: " + task?.taskCategory
        binding.dateText.text = "Date: " + task?.endDate.toString()
        binding.descriptionText.text = "Description: " + task?.description
        binding.statusCheckBox.isChecked = task?.isDone ?: false
        binding.statusCheckBox.text = if (task?.isDone == true) "status (Done)" else "status (Not Done)"
        binding.statusCheckBox.isEnabled = false
        binding.notificationSwitch.isEnabled = false
        binding.notificationSwitch.isChecked = task?.notificationOn ?: false

        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("task", task)
            startActivity(intent)
        }

        binding.deleteButton.setOnClickListener {
            if (task != null) {
                taskViewModel.deleteTask(task)
            }
            val intent = Intent(this, MainActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

//        runOnUiThread {
//            attachmentAdapter.differ.submitList(task?.attachments)
//            binding.attachmentsRecyclerViewer.apply {
//                layoutManager = LinearLayoutManager(this@SingleTaskInfoActivity,
//                    LinearLayoutManager.VERTICAL, false)
//                adapter = attachmentAdapter
//            }
//        }
    }
}
