package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class ViewMode {
    SPLIT,  // مقسم
    EDITOR, // محرر
    PREVIEW // معاينة
}

@Composable
fun TopHeaderBar(
    projectName: String,
    currentViewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenDrawer: () -> Unit,
    onRunCode: () -> Unit,
    onOpenApkBuilder: () -> Unit,
    onCopyCode: () -> Unit,
    onDownloadZip: () -> Unit,
    onShareCode: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Drawer Toggle & Tech Brand Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .testTag("drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "قائمة الملفات",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "</>",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "CodeCanvas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Middle: Sleek View Mode Segmented Controls with Icons
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewModeChip(
                        title = "مقسم",
                        icon = Icons.Default.ViewColumn,
                        isSelected = currentViewMode == ViewMode.SPLIT,
                        onClick = { onViewModeChange(ViewMode.SPLIT) }
                    )
                    ViewModeChip(
                        title = "محرر",
                        icon = Icons.Default.Code,
                        isSelected = currentViewMode == ViewMode.EDITOR,
                        onClick = { onViewModeChange(ViewMode.EDITOR) }
                    )
                    ViewModeChip(
                        title = "معاينة",
                        icon = Icons.Default.Visibility,
                        isSelected = currentViewMode == ViewMode.PREVIEW,
                        onClick = { onViewModeChange(ViewMode.PREVIEW) }
                    )
                }

                // Right: Run Code, Premium Compact APK Badge & Extra Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Run / Execute Action Button
                    IconButton(
                        onClick = onRunCode,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(IdeAccentRun.copy(alpha = 0.2f))
                            .border(1.dp, IdeAccentRun.copy(alpha = 0.6f), CircleShape)
                            .testTag("run_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل الكود",
                            tint = IdeAccentRun,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Premium Compact APK Export Pill
                    Surface(
                        onClick = onOpenApkBuilder,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .height(24.dp)
                            .testTag("export_apk_button")
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1D4ED8), Color(0xFF0284C7))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = "تصدير APK",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "APK",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Options Dropdown Menu for Extra Tools
                    var showExtraMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showExtraMenu = true },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات إضافية",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showExtraMenu,
                            onDismissRequest = { showExtraMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("نسخ الكود", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showExtraMenu = false
                                    onCopyCode()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تحميل ZIP", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showExtraMenu = false
                                    onDownloadZip()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("مشاركة الكود", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showExtraMenu = false
                                    onShareCode()
                                }
                            )
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text(if (isDarkTheme) "الوضع الفاتح" else "الوضع الداكن", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = {
                                    Icon(
                                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showExtraMenu = false
                                    onToggleTheme()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "chipBg"
    )
    val textColor = animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        label = "chipText"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor.value)
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor.value,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor.value
        )
    }
}
