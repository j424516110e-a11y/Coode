package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NewFileDialog(
    parentFolderId: Long?,
    onConfirm: (fileName: String, language: String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("python") }

    val languages = listOf(
        "python" to "Python (.py)",
        "html" to "HTML (.html)",
        "javascript" to "JavaScript (.js)",
        "css" to "CSS (.css)",
        "cpp" to "C++ (.cpp)",
        "dart" to "Flutter / Dart (.dart)",
        "shell" to "Shell (.sh)",
        "json" to "JSON (.json)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء ملف جديد", fontSize = 16.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("اسم الملف (مثال: script.py)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_file_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("اختر لغة البرمجة:", fontSize = 12.sp)

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(languages.firstOrNull { it.first == selectedLang }?.second ?: selectedLang)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedLang = key
                                    if (!fileName.contains(".")) {
                                        val ext = when (key) {
                                            "python" -> ".py"
                                            "html" -> ".html"
                                            "javascript" -> ".js"
                                            "css" -> ".css"
                                            "cpp" -> ".cpp"
                                            "dart" -> ".dart"
                                            "shell" -> ".sh"
                                            "json" -> ".json"
                                            else -> ".txt"
                                        }
                                        if (fileName.isNotBlank()) fileName += ext
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        onConfirm(fileName, selectedLang)
                    }
                },
                modifier = Modifier.testTag("confirm_create_file_button")
            ) {
                Text("إنشاء الملف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun NewFolderDialog(
    parentFolderId: Long?,
    onConfirm: (folderName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء مجلد جديد", fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("اسم المجلد (مثال: utils)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("new_folder_name_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName)
                    }
                },
                modifier = Modifier.testTag("confirm_create_folder_button")
            ) {
                Text("إنشاء المجلد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}
