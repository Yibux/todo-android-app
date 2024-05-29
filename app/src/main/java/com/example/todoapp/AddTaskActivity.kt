package com.example.todoapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.database.Repository
import com.example.todoapp.database.TodoDB
import com.example.todoapp.database.TodoDao
import com.example.todoapp.databinding.AddTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel
import com.google.type.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.UUID


class AddTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener {
    private lateinit var binding: AddTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    private var attachments: MutableList<String> = mutableListOf()
    companion object {
        const val PICK_FILE_REQUEST_CODE = 1
    }

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
            addTask()
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
                    attachments.add(uri.toString())
                }
            } else {
                result.data?.data?.let { uri ->
                    attachments.add(uri.toString())
                }
            }
            binding.attachedFilesText.text = attachments.joinToString("\n")
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

    private fun addTask() {
        val title = binding.titleEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val category = binding.categoryEditText.text.toString()
        val endDate = LocalDate.now()
        //TODO: Add attachments
        //TODO: change enddate to selectedDate
        //val endDate = createDateFromString(binding.endDateEditText.text.toString())
        val notificationEnabled = binding.notificationSwitch.isChecked

        val task = Task(0,
            title,
            description,
            false,
            LocalDate.now(),
            endDate,
            notificationEnabled,
            category,
            attachments
        )

        taskViewModel.addTask(task)

        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}