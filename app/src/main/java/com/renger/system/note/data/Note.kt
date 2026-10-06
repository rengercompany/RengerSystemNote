package com.renger.system.note.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val color: Int = 0xFF6C63FF.toInt(),
    val folderId: Int? = null,
    val tags: String = "",
    val attachments: String = "" // пути через \n
) {
    fun tagList(): List<String> =
        tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun attachmentList(): List<String> =
        attachments.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
}