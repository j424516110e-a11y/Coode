package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_files")
data class CodeFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val parentFolderId: Long? = null, // null = root level, otherwise folder id
    val name: String,
    val isFolder: Boolean = false,
    val language: String = "python", // python, javascript, html, css, dart, cpp, json, markdown, shell
    val content: String = "",
    val isOpenInTab: Boolean = false,
    val tabOrder: Int = 0,
    val isExpandedInTree: Boolean = true
)
