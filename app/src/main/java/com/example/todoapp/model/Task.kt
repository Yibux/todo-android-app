package com.example.todoapp.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Entity(tableName = "tasks")
@Parcelize
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "description")
    var description: String = "",

    @ColumnInfo(name = "is_done")
    var isDone: Boolean = false,

    @ColumnInfo(name = "created_at")
    var createdAt : LocalDate = LocalDate.now(),

    @ColumnInfo(name = "end_date")
    var endDate : LocalDate? = null,

    @ColumnInfo(name = "notification_on")
    var notificationOn : Boolean = false,

    @ColumnInfo(name = "task_category")
    var taskCategory : String = "",

    @ColumnInfo(name = "attachments")
    var attachments : List<String>? = null
) : Parcelable
