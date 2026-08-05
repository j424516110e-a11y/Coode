package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ApkBuildDao {
    @Query("SELECT * FROM apk_builds WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getBuildsForProject(projectId: Long): Flow<List<ApkBuildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildRecord(build: ApkBuildEntity): Long

    @Update
    suspend fun updateBuildRecord(build: ApkBuildEntity)

    @Delete
    suspend fun deleteBuildRecord(build: ApkBuildEntity)
}
