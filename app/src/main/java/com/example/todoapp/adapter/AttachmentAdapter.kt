package com.example.todoapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.AttachmentFragmentBinding


class AttachmentAdapter: RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    private lateinit var binding : AttachmentFragmentBinding
    var onAttachmentDeletedListener: OnAttachmentDeletedListener? = null
    inner class ViewHolder : RecyclerView.ViewHolder(binding.root)

    interface OnAttachmentDeletedListener {
        fun onAttachmentDeleted(id: Int)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttachmentAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = AttachmentFragmentBinding.inflate(inflater, parent, false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: AttachmentAdapter.ViewHolder, position: Int) {
        val binding = AttachmentFragmentBinding.bind(holder.itemView)
        val attachment: String? = differ.currentList[position]

        binding.attachmentText.text = attachment

        binding.deleteAttachmentButton.setOnClickListener {
            differ.currentList.removeAt(position)
            onAttachmentDeletedListener?.onAttachmentDeleted(position)
        }
    }

    override fun getItemCount() = differ.currentList.size

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)
}