package com.example.todoapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.adapter.AttachmentAdapter
import com.example.todoapp.databinding.EditTaskActivityBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel
import java.time.LocalDate

class EditTaskActivity : AppCompatActivity(), DatePickerFragment.OnDateSelectedListener,
    AttachmentAdapter.OnAttachmentDeletedListener {
    private lateinit var binding: EditTaskActivityBinding
    private lateinit var taskViewModel: TaskViewModel
    private var selectedDate: LocalDate = LocalDate.now()
    private lateinit var attachments: MutableList<String>
    private val attachmentAdapter by lazy { AttachmentAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditTaskActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        attachmentAdapter.onAttachmentDeletedListener = this

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val task: Task? = intent.getParcelableExtra("task", Task::class.java)

        binding.titleEditText.addTextChangedListener(textWatcher)
        binding.descriptionEditText.addTextChangedListener(textWatcher)
        binding.categoryEditText.addTextChangedListener(textWatcher)
        binding.dateText.addTextChangedListener(textWatcher)
        activeTaskButton()

        binding.titleEditText.setText(task?.title)
        binding.categoryEditText.setText(task?.taskCategory)
        binding.dateText.text = task?.endDate.toString()
        binding.descriptionEditText.setText(task?.description)
        binding.statusCheckBox.isChecked = task?.isDone ?: false
        binding.statusCheckBox.text = if (task?.isDone == true) "status (Done)" else "status (Not Done)"
        attachments = task?.attachments?.toMutableList() ?: mutableListOf()

        binding.editTaskButton.setOnClickListener {
            editTask(task)
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

        updateUI()
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

    private fun editTask(task: Task?) {
        if (task == null) {
            return
        }
        task.title = binding.titleEditText.text.toString()
        task.taskCategory = binding.categoryEditText.text.toString()
        task.endDate = selectedDate
        task.description = binding.descriptionEditText.text.toString()
        task.isDone = binding.statusCheckBox.isChecked
        task.attachments = attachments
        taskViewModel.updateTask(task)

        val intent = Intent(this, SingleTaskInfoActivity::class.java)
        intent.putExtra("task", task)
//        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
    }

    override fun onAttachmentDeleted(id: Int) {
        attachments.removeAt(id)
        updateUI()
    }
}
