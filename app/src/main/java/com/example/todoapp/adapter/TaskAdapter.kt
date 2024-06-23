package com.example.todoapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.EditTaskActivity
import com.example.todoapp.SingleTaskInfoActivity
import com.example.todoapp.databinding.TaskLayoutBinding
import com.example.todoapp.model.Task
import com.example.todoapp.viewmodel.TaskViewModel
import kotlinx.coroutines.withContext

class TaskAdapter(private val taskViewModel: TaskViewModel) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private lateinit var binding : TaskLayoutBinding
    inner class ViewHolder : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = TaskLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = TaskLayoutBinding.bind(holder.itemView)
        val task = differ.currentList[position]
        binding.titleTextView.text = task.title
        binding.endDate.text = task.endDate.toString().replace("T", " ")
        binding.categoryTextView.text = task.taskCategory
        setIsCompletedImage(binding, task.isDone)

        if(task.attachments!!.size >= 0 && task.attachments!![0].isNotEmpty()) {
            binding.attachmentIcon.setImageResource(android.R.drawable.stat_notify_sdcard)
        } else {
            binding.attachmentIcon.visibility = android.view.View.GONE
        }

        binding.isCompleted.setOnClickListener {
            task.isDone = !task.isDone
            setIsCompletedImage(binding, task.isDone)
            taskViewModel.updateTask(task)
        }

        binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SingleTaskInfoActivity::class.java).apply {
                putExtra("task_id", task.id)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    private fun setIsCompletedImage(binding: TaskLayoutBinding, isDone: Boolean) {
        if(isDone) {
            binding.isCompleted.setImageResource(android.R.drawable.checkbox_on_background)
        } else {
            binding.isCompleted.setImageResource(android.R.drawable.checkbox_off_background)
        }
    }

    override fun getItemCount() = differ.currentList.size

    private val diffCallback = object : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)
}