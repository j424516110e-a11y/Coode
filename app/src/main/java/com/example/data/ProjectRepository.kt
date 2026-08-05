package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val codeFileDao: CodeFileDao,
    private val apkBuildDao: ApkBuildDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>> =
        codeFileDao.getFilesForProject(projectId)

    fun getOpenTabsForProject(projectId: Long): Flow<List<CodeFileEntity>> =
        codeFileDao.getOpenTabsForProject(projectId)

    fun getBuildsForProject(projectId: Long): Flow<List<ApkBuildEntity>> =
        apkBuildDao.getBuildsForProject(projectId)

    suspend fun getProjectById(id: Long): ProjectEntity? =
        projectDao.getProjectById(id)

    suspend fun getFilesForProjectOnce(projectId: Long): List<CodeFileEntity> =
        codeFileDao.getFilesForProjectOnce(projectId)

    suspend fun getFileById(fileId: Long): CodeFileEntity? =
        codeFileDao.getFileById(fileId)

    suspend fun insertProject(project: ProjectEntity): Long =
        projectDao.insertProject(project)

    suspend fun updateProject(project: ProjectEntity) =
        projectDao.updateProject(project)

    suspend fun deleteProject(project: ProjectEntity) {
        codeFileDao.deleteFilesForProject(project.id)
        projectDao.deleteProject(project)
    }

    suspend fun insertFile(file: CodeFileEntity): Long =
        codeFileDao.insertFile(file)

    suspend fun updateFile(file: CodeFileEntity) =
        codeFileDao.updateFile(file)

    suspend fun deleteFile(fileId: Long) =
        codeFileDao.deleteFileOrFolder(fileId)

    suspend fun insertBuildRecord(build: ApkBuildEntity): Long =
        apkBuildDao.insertBuildRecord(build)

    suspend fun seedInitialProjectsIfEmpty() {
        // Seeding logic called from ViewModel on app launch if empty
    }
}
