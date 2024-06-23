package com.example.todoapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.AttachmentFragmentBinding
import java.io.File


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
        val attachmentSplit = attachment?.split("\n")

        binding.attachmentText.text = attachmentSplit?.get(1)

        binding.deleteAttachmentButton.setOnClickListener {
            val currentList = differ.currentList.toMutableList()
            if (position in currentList.indices) {
                val attachmentUri = currentList[position].split("\n")[0].replace("file:","")
                val file = File(attachmentUri)
                val deleteSuccess = file.delete()

                if (!deleteSuccess) {
                    Log.e("File Deletion", "Failed to delete file: $attachmentUri")
                } else {
                    Log.d("File Deleted", "File $attachmentUri successfully deleted.")
                }

                currentList.removeAt(position)
                differ.submitList(currentList)
                onAttachmentDeletedListener?.onAttachmentDeleted(position)
            }
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