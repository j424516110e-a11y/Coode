package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val primaryLanguage: String = "python", // python, javascript, html, dart, cpp
    val activeFileId: Long = 0,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val modifiedTimestamp: Long = System.currentTimeMillis(),
    
    // APK Configuration
    val apkAppName: String = "My Application",
    val apkPackageName: String = "com.aistudio.generated.app",
    val apkVersionName: String = "1.0.0",
    val apkVersionCode: Int = 1,
    val enablePushNotifications: Boolean = true,
    val enableCameraAccess: Boolean = true,
    val enableStorageAccess: Boolean = true,
    val enableGpsAccess: Boolean = false,
    val enableMicrophoneAccess: Boolean = false,
    val customIconRes: String = "ic_launcher"
)
