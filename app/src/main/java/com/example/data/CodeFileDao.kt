package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeFileDao {
    @Query("SELECT * FROM code_files WHERE projectId = :projectId ORDER BY isFolder DESC, name ASC")
    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>>

    @Query("SELECT * FROM code_files WHERE projectId = :projectId")
    suspend fun getFilesForProjectOnce(projectId: Long): List<CodeFileEntity>

    @Query("SELECT * FROM code_files WHERE id = :fileId")
    suspend fun getFileById(fileId: Long): CodeFileEntity?

    @Query("SELECT * FROM code_files WHERE projectId = :projectId AND isOpenInTab = 1 ORDER BY tabOrder ASC")
    fun getOpenTabsForProject(projectId: Long): Flow<List<CodeFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CodeFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<CodeFileEntity>): List<Long>

    @Update
    suspend fun updateFile(file: CodeFileEntity)

    @Delete
    suspend fun deleteFile(file: CodeFileEntity)

    @Query("DELETE FROM code_files WHERE id = :fileId OR parentFolderId = :fileId")
    suspend fun deleteFileOrFolder(fileId: Long)

    @Query("DELETE FROM code_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: Long)
}
