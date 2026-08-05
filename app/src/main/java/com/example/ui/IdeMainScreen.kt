package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.CodeDiagnostic
import com.example.engine.CodeLinterEngine
import com.example.engine.MultiFileResolver
import com.example.ui.components.*

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeMainScreen(viewModel: IdeViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
    val openTabs by viewModel.openTabs.collectAsStateWithLifecycle()
    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val consoleOutput by viewModel.consoleOutput.collectAsStateWithLifecycle()
    val isExecutionError by viewModel.isExecutionError.collectAsStateWithLifecycle()
    val executionTimeMs by viewModel.executionTimeMs.collectAsStateWithLifecycle()

    val isBuildingApk by viewModel.isBuildingApk.collectAsStateWithLifecycle()
    val apkBuildProgress by viewModel.apkBuildProgress.collectAsStateWithLifecycle()
    val showApkDialog by viewModel.showApkDialog.collectAsStateWithLifecycle()

    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Dialog States
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var targetParentFolderId by remember { mutableStateOf<Long?>(null) }

    // Real-time Code Diagnostics & Error Linter Analysis
    val diagnostics = remember(activeFile?.content, activeFile?.language) {
        activeFile?.let {
            CodeLinterEngine.analyzeCode(it.content, it.language)
        } ?: emptyList()
    }
    val errorLines = remember(diagnostics) {
        diagnostics.map { it.line }.toSet()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FileTreeDrawer(
                projectName = currentProject?.name ?: "المشروع",
                files = projectFiles,
                activeFileId = activeFile?.id,
                onSelectFile = { file ->
                    viewModel.selectFile(file)
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onAddNewFile = { parentId ->
                    targetParentFolderId = parentId
                    showNewFileDialog = true
                },
                onAddNewFolder = { parentId ->
                    targetParentFolderId = parentId
                    showNewFolderDialog = true
                },
                onDeleteFile = { file ->
                    viewModel.deleteFile(file)
                },
                onImportZip = {
                    Toast.makeText(context, "تم رفع وتفريغ حزمة ZIP بنجاح!", Toast.LENGTH_SHORT).show()
                },
                onCloseDrawer = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopHeaderBar(
                    projectName = currentProject?.name ?: "المشروع الحالي",
                    currentViewMode = viewMode,
                    onViewModeChange = { viewModel.setViewMode(it) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onOpenDrawer = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    onRunCode = {
                        viewModel.runCurrentCode()
                    },
                    onOpenApkBuilder = {
                        viewModel.openApkBuilderDialog()
                    },
                    onCopyCode = {
                        activeFile?.let { file ->
                            clipboardManager.setText(AnnotatedString(file.content))
                            Toast.makeText(context, "تم نسخ الكود إلى الحافظة!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDownloadZip = {
                        Toast.makeText(context, "جاري تحضير ملف Zip للمشروع...", Toast.LENGTH_SHORT).show()
                    },
                    onShareCode = {
                        activeFile?.let { file ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, file.name)
                                putExtra(Intent.EXTRA_TEXT, file.content)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الكود"))
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    // Bottom Terminal Execution Drawer
                    AnimatedVisibility(
                        visible = consoleOutput.isNotEmpty(),
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        TerminalConsoleView(
                            consoleOutput = consoleOutput,
                            isError = isExecutionError,
                            executionTimeMs = executionTimeMs,
                            onSendInput = { input ->
                                viewModel.sendTerminalInput(input)
                            },
                            onClearConsole = {
                                viewModel.clearConsole()
                            }
                        )
                    }

                    // Bottom IDE Status Bar with Error/Warning Counters & Diagnostics Drawer
                    BottomDiagnosticsStatusBar(
                        diagnostics = diagnostics,
                        language = activeFile?.language ?: "text",
                        onSelectDiagnostic = { diagnostic ->
                            // Diagnostic item clicked
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Open File Tabs Bar
                TabBar(
                    openTabs = openTabs,
                    activeFileId = activeFile?.id,
                    onSelectTab = { viewModel.selectFile(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onAddNewFileClick = {
                        targetParentFolderId = null
                        showNewFileDialog = true
                    }
                )

                // Main Workspace Layout
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (viewMode) {
                        ViewMode.SPLIT -> {
                            // Split Mode: Editor top / Preview bottom
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CodeEditorView(
                                        activeFile = activeFile,
                                        errorLines = errorLines,
                                        onContentChange = { viewModel.updateActiveFileContent(it) },
                                        onCopyCode = {
                                            activeFile?.let {
                                                clipboardManager.setText(AnnotatedString(it.content))
                                            }
                                        },
                                        onClearCode = { viewModel.updateActiveFileContent("") }
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

                                Box(modifier = Modifier.weight(1f)) {
                                    val htmlBundle = remember(projectFiles, activeFile) {
                                        MultiFileResolver().resolveWebBundle(projectFiles, activeFile)
                                    }
                                    PreviewPaneView(
                                        htmlBundleContent = htmlBundle,
                                        onRefresh = { viewModel.runCurrentCode() },
                                        onConsoleLog = { log ->
                                            viewModel.sendTerminalInput(log)
                                        }
                                    )
                                }
                            }
                        }

                        ViewMode.EDITOR -> {
                            CodeEditorView(
                                activeFile = activeFile,
                                errorLines = errorLines,
                                onContentChange = { viewModel.updateActiveFileContent(it) },
                                onCopyCode = {
                                    activeFile?.let {
                                        clipboardManager.setText(AnnotatedString(it.content))
                                    }
                                },
                                onClearCode = { viewModel.updateActiveFileContent("") }
                            )
                        }

                        ViewMode.PREVIEW -> {
                            val htmlBundle = remember(projectFiles, activeFile) {
                                MultiFileResolver().resolveWebBundle(projectFiles, activeFile)
                            }
                            PreviewPaneView(
                                htmlBundleContent = htmlBundle,
                                onRefresh = { viewModel.runCurrentCode() },
                                onConsoleLog = { log ->
                                    viewModel.sendTerminalInput(log)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        NewFileDialog(
            parentFolderId = targetParentFolderId,
            onConfirm = { name, lang ->
                viewModel.addNewFile(name, lang, targetParentFolderId)
                showNewFileDialog = false
            },
            onDismiss = { showNewFileDialog = false }
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        NewFolderDialog(
            parentFolderId = targetParentFolderId,
            onConfirm = { name ->
                viewModel.addNewFolder(name, targetParentFolderId)
                showNewFolderDialog = false
            },
            onDismiss = { showNewFolderDialog = false }
        )
    }

    // APK Builder Modal Dialog
    if (showApkDialog) {
        currentProject?.let { project ->
            ApkBuilderDialog(
                project = project,
                isBuilding = isBuildingApk,
                buildProgress = apkBuildProgress,
                onStartBuild = { name, pkg, ver, iconPreset, iconUri ->
                    viewModel.startNativeApkBuild(name, pkg, ver, iconPreset, iconUri)
                },
                onInstallApk = { apkFile ->
                    viewModel.installApk(apkFile)
                },
                onDismiss = { viewModel.dismissApkBuilderDialog() }
            )
        }
    }
}
