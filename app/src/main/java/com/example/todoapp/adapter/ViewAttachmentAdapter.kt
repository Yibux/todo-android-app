package com.example.todoapp.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.SingleTaskInfoActivity
import com.example.todoapp.databinding.ViewAttachmentFragmentBinding
import java.io.File

class ViewAttachmentAdapter: RecyclerView.Adapter<ViewAttachmentAdapter.ViewHolder>() {
    private lateinit var binding: ViewAttachmentFragmentBinding
    inner class ViewHolder: RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ViewAttachmentFragmentBinding.inflate(inflater, parent, false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < differ.currentList.size) {
            val attachment: String = differ.currentList[position]
            val attachmentSplit = attachment.split("\n")
            val attachmentUri = attachmentSplit[0].replace("file://", "")
            val fileName = attachmentSplit[1]

            binding.attachmentText.text = fileName

            holder.itemView.setOnClickListener {
                val file = File(attachmentUri)

                val contentUri = FileProvider.getUriForFile(
                    holder.itemView.context,
                    "${holder.itemView.context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    setDataAndType(contentUri, "*/*")
                }

                startActivity(holder.itemView.context, intent, null)
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
