package com.example.todoapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.TaskLayoutBinding
import com.example.todoapp.model.Task

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

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
        binding.endDate.text = task.endDate.toString()

        binding.root.setOnClickListener {

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

    private val differ = AsyncListDiffer(this, diffCallback)
}