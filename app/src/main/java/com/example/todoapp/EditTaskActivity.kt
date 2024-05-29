package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.databinding.EditTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel
import java.time.LocalDate

class EditTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener {
    private lateinit var binding: EditTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditTaskActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val task: Task? = intent.getParcelableExtra("task", Task::class.java)

        binding.titleEditText.setText(task?.title)
        binding.categoryEditText.setText(task?.taskCategory)
        binding.dateText.text = task?.endDate.toString()
        binding.descriptionEditText.setText(task?.description)
        binding.statusCheckBox.isChecked = task?.isDone ?: false
        binding.statusCheckBox.text = if (task?.isDone == true) "status (Done)" else "status (Not Done)"
        //TODO: Add attachments
        //TODO: Add notifications



        binding.editTaskButton.setOnClickListener {
            editTask(task)
        }

        binding.pickDate.setOnClickListener {
            val datePicker = DatePickerFragment()
            datePicker.show(supportFragmentManager, "datePicker")
        }
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        selectedDate = LocalDate.of(year, month + 1, day)
        binding.dateText.text = selectedDate.toString()
    }

    private fun editTask(task : Task?) {
        if (task == null) {
            return
        }
        task.title = binding.titleEditText.text.toString()
        task.taskCategory = binding.categoryEditText.text.toString()
        task.endDate = selectedDate
        task.description = binding.descriptionEditText.text.toString()
        task.isDone = binding.statusCheckBox.isChecked
        taskViewModel.updateTask(task)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}