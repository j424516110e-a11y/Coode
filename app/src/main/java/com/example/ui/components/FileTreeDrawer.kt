package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun FileTreeDrawer(
    projectName: String,
    files: List<CodeFileEntity>,
    activeFileId: Long?,
    onSelectFile: (CodeFileEntity) -> Unit,
    onAddNewFile: (parentFolderId: Long?) -> Unit,
    onAddNewFolder: (parentFolderId: Long?) -> Unit,
    onDeleteFile: (CodeFileEntity) -> Unit,
    onImportZip: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Group root items (parentFolderId == null)
    val rootItems = remember(filteredFiles) {
        filteredFiles.filter { it.parentFolderId == null }
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = "شجرة الملفات",
                        tint = IdePrimarySky
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مستكشف المشروع",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.testTag("close_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: + ملف, + مجلد, ZIP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onAddNewFile(null) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawer_add_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("ملف", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onAddNewFolder(null) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawer_add_folder")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("مجلد", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onImportZip,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("drawer_import_zip")
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "رفع ZIP",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Filter Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن ملف...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("file_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // File Tree List View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(rootItems, key = { it.id }) { item ->
                    FileTreeNodeItem(
                        node = item,
                        allFiles = filteredFiles,
                        activeFileId = activeFileId,
                        depth = 0,
                        onSelectFile = onSelectFile,
                        onAddNewFileInFolder = { onAddNewFile(it.id) },
                        onAddNewFolderInFolder = { onAddNewFolder(it.id) },
                        onDelete = onDeleteFile
                    )
                }
            }
        }
    }
}

@Composable
private fun FileTreeNodeItem(
    node: CodeFileEntity,
    allFiles: List<CodeFileEntity>,
    activeFileId: Long?,
    depth: Int,
    onSelectFile: (CodeFileEntity) -> Unit,
    onAddNewFileInFolder: (CodeFileEntity) -> Unit,
    onAddNewFolderInFolder: (CodeFileEntity) -> Unit,
    onDelete: (CodeFileEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(node.isExpandedInTree) }
    val children = remember(allFiles, node.id) {
        allFiles.filter { it.parentFolderId == node.id }
    }
    val isActive = node.id == activeFileId && !node.isFolder

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isActive) IdePrimarySky.copy(alpha = 0.2f) else Color.Transparent
                )
                .clickable {
                    if (node.isFolder) {
                        isExpanded = !isExpanded
                    } else {
                        onSelectFile(node)
                    }
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (node.isFolder) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = IdePrimarySky,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = getFileIconEmoji(node.name),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = node.name,
                    fontSize = 13.sp,
                    fontWeight = if (isActive || node.isFolder) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) IdePrimarySky else MaterialTheme.colorScheme.onSurface
                )
            }

            // Options menu for folder/file
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (node.isFolder) {
                        DropdownMenuItem(
                            text = { Text("+ ملف داخل المجلد") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onAddNewFileInFolder(node)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("+ مجلد فرعي") },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onAddNewFolderInFolder(node)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete(node)
                        }
                    )
                }
            }
        }

        // Render sub-tree children recursively
        if (node.isFolder && isExpanded) {
            children.forEach { child ->
                FileTreeNodeItem(
                    node = child,
                    allFiles = allFiles,
                    activeFileId = activeFileId,
                    depth = depth + 1,
                    onSelectFile = onSelectFile,
                    onAddNewFileInFolder = onAddNewFileInFolder,
                    onAddNewFolderInFolder = onAddNewFolderInFolder,
                    onDelete = onDelete
                )
            }
        }
    }
}
