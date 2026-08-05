package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apk_builds")
data class ApkBuildEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val appName: String,
    val packageName: String,
    val versionName: String,
    val apkFilePath: String,
    val apkSizeBytes: Long,
    val buildStatus: String, // SUCCESS, FAILED, BUILDING
    val buildLogs: String,
    val includeNotifications: Boolean = true
)
