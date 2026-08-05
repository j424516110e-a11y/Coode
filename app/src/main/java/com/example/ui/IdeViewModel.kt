package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.engine.*
import com.example.ui.components.ViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(db.projectDao(), db.codeFileDao(), db.apkBuildDao())
    private val executionEngine = ExecutionEngine()
    private val apkPipeline = ApkCompilerPipeline(application)

    // UI States
    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<CodeFileEntity>>(emptyList())
    val projectFiles: StateFlow<List<CodeFileEntity>> = _projectFiles.asStateFlow()

    private val _openTabs = MutableStateFlow<List<CodeFileEntity>>(emptyList())
    val openTabs: StateFlow<List<CodeFileEntity>> = _openTabs.asStateFlow()

    private val _activeFile = MutableStateFlow<CodeFileEntity?>(null)
    val activeFile: StateFlow<CodeFileEntity?> = _activeFile.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.SPLIT)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Execution & Terminal State
    private val _consoleOutput = MutableStateFlow("")
    val consoleOutput: StateFlow<String> = _consoleOutput.asStateFlow()

    private val _isExecutionError = MutableStateFlow(false)
    val isExecutionError: StateFlow<Boolean> = _isExecutionError.asStateFlow()

    private val _executionTimeMs = MutableStateFlow(0L)
    val executionTimeMs: StateFlow<Long> = _executionTimeMs.asStateFlow()

    // APK Building State
    private val _isBuildingApk = MutableStateFlow(false)
    val isBuildingApk: StateFlow<Boolean> = _isBuildingApk.asStateFlow()

    private val _apkBuildProgress = MutableStateFlow<BuildStepProgress?>(null)
    val apkBuildProgress: StateFlow<BuildStepProgress?> = _apkBuildProgress.asStateFlow()

    private val _showApkDialog = MutableStateFlow(false)
    val showApkDialog: StateFlow<Boolean> = _showApkDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allProjects.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultProjects()
                } else if (_currentProject.value == null) {
                    selectProject(list.first())
                }
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        _currentProject.value = project
        viewModelScope.launch {
            repository.getFilesForProject(project.id).collect { files ->
                _projectFiles.value = files
                val tabs = files.filter { it.isOpenInTab }
                _openTabs.value = tabs

                val active = files.firstOrNull { it.id == project.activeFileId }
                    ?: tabs.firstOrNull()
                    ?: files.firstOrNull { !it.isFolder }
                _activeFile.value = active
            }
        }
    }

    fun selectFile(file: CodeFileEntity) {
        if (file.isFolder) return
        _activeFile.value = file
        viewModelScope.launch {
            val updated = file.copy(isOpenInTab = true)
            repository.updateFile(updated)
            _currentProject.value?.let { p ->
                repository.updateProject(p.copy(activeFileId = file.id, modifiedTimestamp = System.currentTimeMillis()))
            }
        }
    }

    fun updateActiveFileContent(newContent: String) {
        val file = _activeFile.value ?: return
        val updated = file.copy(content = newContent)
        _activeFile.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFile(updated)
        }
    }

    fun closeTab(file: CodeFileEntity) {
        viewModelScope.launch {
            val updated = file.copy(isOpenInTab = false)
            repository.updateFile(updated)
            if (_activeFile.value?.id == file.id) {
                val remainingTabs = _openTabs.value.filter { it.id != file.id }
                _activeFile.value = remainingTabs.firstOrNull()
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun runCurrentCode(userInput: String = "") {
        val file = _activeFile.value ?: return
        val allFiles = _projectFiles.value

        viewModelScope.launch {
            val result = executionEngine.executeCode(file, allFiles, userInput)
            _consoleOutput.value = result.output
            _isExecutionError.value = result.isError
            _executionTimeMs.value = result.executionTimeMs
        }
    }

    fun sendTerminalInput(input: String) {
        _consoleOutput.value += "\n>>> $input\n"
        runCurrentCode(userInput = input)
    }

    fun clearConsole() {
        _consoleOutput.value = ""
        _isExecutionError.value = false
        _executionTimeMs.value = 0L
    }

    fun addNewFile(name: String, language: String, parentFolderId: Long?) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val newFile = CodeFileEntity(
                projectId = project.id,
                parentFolderId = parentFolderId,
                name = name,
                isFolder = false,
                language = language,
                content = getDefaultContentForLanguage(name, language),
                isOpenInTab = true
            )
            val id = repository.insertFile(newFile)
            selectFile(newFile.copy(id = id))
        }
    }

    fun addNewFolder(name: String, parentFolderId: Long?) {
        val project = _currentProject.value ?: return
        viewModelScope.launch {
            val newFolder = CodeFileEntity(
                projectId = project.id,
                parentFolderId = parentFolderId,
                name = name,
                isFolder = true,
                language = "folder",
                content = ""
            )
            repository.insertFile(newFolder)
        }
    }

    fun deleteFile(file: CodeFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file.id)
            if (_activeFile.value?.id == file.id) {
                _activeFile.value = _openTabs.value.firstOrNull { it.id != file.id }
            }
        }
    }

    fun openApkBuilderDialog() {
        _showApkDialog.value = true
    }

    fun dismissApkBuilderDialog() {
        _showApkDialog.value = false
    }

    fun startNativeApkBuild(
        appName: String,
        packageName: String,
        versionName: String,
        appIconPreset: String = "ic_launcher",
        appIconUri: String? = null
    ) {
        val project = _currentProject.value ?: return
        val iconResName = appIconUri ?: appIconPreset
        val updatedProject = project.copy(
            apkAppName = appName,
            apkPackageName = packageName,
            apkVersionName = versionName,
            customIconRes = iconResName
        )

        _currentProject.value = updatedProject
        _isBuildingApk.value = true

        viewModelScope.launch {
            repository.updateProject(updatedProject)
            val files = repository.getFilesForProjectOnce(project.id)

            apkPipeline.compileAndExportApk(updatedProject, files).collect { progress ->
                _apkBuildProgress.value = progress
                if (progress.isFinished) {
                    _isBuildingApk.value = false
                    progress.generatedApkFile?.let { apkFile ->
                        repository.insertBuildRecord(
                            ApkBuildEntity(
                                projectId = project.id,
                                appName = appName,
                                packageName = packageName,
                                versionName = versionName,
                                apkFilePath = apkFile.absolutePath,
                                apkSizeBytes = apkFile.length(),
                                buildStatus = "SUCCESS",
                                buildLogs = progress.logMessage,
                                includeNotifications = true
                            )
                        )
                    }
                }
            }
        }
    }

    fun installApk(file: File) {
        apkPipeline.triggerApkInstallation(file)
    }

    private suspend fun seedDefaultProjects() {
        // Project 1: HTML5 Fullstack App with Push Notification demo
        val proj1Id = repository.insertProject(
            ProjectEntity(
                name = "تطبيق ويب متكامل (HTML5 & JS)",
                description = "مشروع موقع وتطبيق تفاعلي يدعم الإشعارات والمعاينة المباشرة",
                primaryLanguage = "html",
                apkAppName = "CodeCanvas WebApp",
                apkPackageName = "com.aistudio.codecanvas.webapp"
            )
        )

        // Subfolder: js
        val jsFolderId = repository.insertFile(
            CodeFileEntity(projectId = proj1Id, name = "js", isFolder = true)
        )
        // Subfolder: css
        val cssFolderId = repository.insertFile(
            CodeFileEntity(projectId = proj1Id, name = "css", isFolder = true)
        )

        // Files
        repository.insertFile(
            CodeFileEntity(
                projectId = proj1Id,
                name = "index.html",
                language = "html",
                content = """
                    <!DOCTYPE html>
                    <html lang="ar" dir="rtl">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>CodeCanvas App</title>
                        <link rel="stylesheet" href="css/style.css">
                    </head>
                    <body>
                        <div class="container">
                            <div class="badge">تطبيق أصلي 100%</div>
                            <h1>مرحباً بك في CodeCanvas IDE 🚀</h1>
                            <p>هذا كود تفاعلي مربوط بين مجلدات متعددة (HTML + CSS + JS).</p>
                            
                            <button id="notifBtn" onclick="sendNotification()">تجربة الإشعارات (Push Notification)</button>
                            <div id="output" class="log-box">جاهز لاستقبال التفاعلات...</div>
                        </div>
                        <script src="js/app.js"></script>
                    </body>
                    </html>
                """.trimIndent(),
                isOpenInTab = true
            )
        )

        repository.insertFile(
            CodeFileEntity(
                projectId = proj1Id,
                parentFolderId = cssFolderId,
                name = "style.css",
                language = "css",
                content = """
                    body {
                        font-family: 'Segoe UI', Tahoma, system-ui, sans-serif;
                        background: linear-gradient(135deg, #0f172a, #1e1b4b);
                        color: #f8fafc;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                    }
                    .container {
                        background: rgba(30, 41, 59, 0.8);
                        backdrop-filter: blur(10px);
                        border: 1px solid rgba(255, 255, 255, 0.1);
                        padding: 32px;
                        border-radius: 20px;
                        text-align: center;
                        max-width: 90%;
                    }
                    .badge {
                        background: #8b5cf6;
                        color: #fff;
                        padding: 4px 12px;
                        border-radius: 20px;
                        font-size: 12px;
                        display: inline-block;
                        margin-bottom: 12px;
                    }
                    button {
                        background: linear-gradient(90deg, #38bdf8, #818cf8);
                        color: white;
                        border: none;
                        padding: 12px 24px;
                        font-size: 14px;
                        font-weight: bold;
                        border-radius: 12px;
                        cursor: pointer;
                    }
                    .log-box {
                        margin-top: 16px;
                        background: #090d12;
                        padding: 12px;
                        border-radius: 8px;
                        color: #38bdf8;
                        font-family: monospace;
                    }
                """.trimIndent(),
                isOpenInTab = true
            )
        )

        repository.insertFile(
            CodeFileEntity(
                projectId = proj1Id,
                parentFolderId = jsFolderId,
                name = "app.js",
                language = "javascript",
                content = """
                    console.log("تطبيق CodeCanvas يعمل بنجاح!");

                    function sendNotification() {
                        const out = document.getElementById("output");
                        out.innerText = "🔔 تم إرسال إشعار تجريبي للتطبيق الأصلي!";
                        console.log("Push notification trigger initiated.");
                    }
                """.trimIndent(),
                isOpenInTab = true
            )
        )

        // Project 2: Python Data & Multi-module Automation Pipeline
        val proj2Id = repository.insertProject(
            ProjectEntity(
                name = "مشروع بايثون متكامل (Python Multi-file)",
                description = "ربط وحدات بايثون متعددة وحسابات متطورة مع موجه المخرجات",
                primaryLanguage = "python",
                apkAppName = "Python Automation",
                apkPackageName = "com.aistudio.codecanvas.python"
            )
        )

        val utilsFolderId = repository.insertFile(
            CodeFileEntity(projectId = proj2Id, name = "utils", isFolder = true)
        )

        repository.insertFile(
            CodeFileEntity(
                projectId = proj2Id,
                name = "main.py",
                language = "python",
                content = """
                    # --- CodeCanvas Python Engine ---
                    from utils.math_helper import calculate_stats

                    print("🚀 بدء تشغيل مشروع بايثون المتكامل...")
                    data = [10, 20, 30, 40, 50]
                    result = calculate_stats(data)

                    print("قائمة البيانات:", data)
                    print("المجموع:", result["sum"])
                    print("المتوسط:", result["avg"])
                    print("العملية تمت بنجاح 100%!")
                """.trimIndent(),
                isOpenInTab = true
            )
        )

        repository.insertFile(
            CodeFileEntity(
                projectId = proj2Id,
                parentFolderId = utilsFolderId,
                name = "math_helper.py",
                language = "python",
                content = """
                    def calculate_stats(numbers):
                        total = sum(numbers)
                        average = total / len(numbers) if numbers else 0
                        return {"sum": total, "avg": average}
                """.trimIndent(),
                isOpenInTab = false
            )
        )
    }

    private fun getDefaultContentForLanguage(fileName: String, lang: String): String {
        return when (lang) {
            "python" -> "# $fileName\n\nprint('Hello from CodeCanvas Python!')\n"
            "html" -> "<!DOCTYPE html>\n<html>\n<head>\n    <title>$fileName</title>\n</head>\n<body>\n    <h1>$fileName</h1>\n</body>\n</html>"
            "javascript" -> "// $fileName\nconsole.log('Running $fileName...');\n"
            "css" -> "/* $fileName */\nbody {\n    background-color: #0f172a;\n}\n"
            "cpp" -> "#include <iostream>\n\nint main() {\n    std::cout << \"Hello World from C++!\" << std::endl;\n    return 0;\n}\n"
            "dart" -> "// Flutter / Dart\nvoid main() {\n  print('Hello from Dart!');\n}\n"
            else -> "// $fileName\n"
        }
    }
}
