package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeFileEntity
import com.example.ui.theme.IdePrimarySky

@Composable
fun TabBar(
    openTabs: List<CodeFileEntity>,
    activeFileId: Long?,
    onSelectTab: (CodeFileEntity) -> Unit,
    onCloseTab: (CodeFileEntity) -> Unit,
    onAddNewFileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        openTabs.forEach { file ->
            val isActive = file.id == activeFileId
            TabItem(
                file = file,
                isActive = isActive,
                onSelect = { onSelectTab(file) },
                onClose = { onCloseTab(file) }
            )
        }

        // Plus (+) button to add new file directly
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onAddNewFileClick() }
                .testTag("add_file_tab_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "ملف جديد",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TabItem(
    file: CodeFileEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val bgColor = if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent
    val textColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val indicatorColor = if (isActive) IdePrimarySky else Color.Transparent

    Column(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(bgColor)
            .clickable { onSelect() }
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // File Icon
            Text(
                text = getFileIconEmoji(file.name),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = file.name,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Close tab icon (✕)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق التبويب",
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Active tab bottom bar indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(indicatorColor)
        )
    }
}

fun getFileIconEmoji(fileName: String): String {
    return when {
        fileName.endsWith(".py") -> "🐍"
        fileName.endsWith(".js") -> "📜"
        fileName.endsWith(".html") -> "🌐"
        fileName.endsWith(".css") -> "🎨"
        fileName.endsWith(".dart") -> "🎯"
        fileName.endsWith(".cpp") || fileName.endsWith(".c") -> "⚙️"
        fileName.endsWith(".sh") -> "🐚"
        fileName.endsWith(".json") -> "📊"
        fileName.endsWith(".md") -> "📝"
        else -> "📄"
    }
}
