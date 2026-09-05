package com.videodownloader.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert
    suspend fun insert(entity: DownloadEntity): Long

    @Update
    suspend fun update(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: Long): DownloadEntity?

    @Query("UPDATE downloads SET downloadedBytes = :downloaded, totalBytes = :total, status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(id: Long, downloaded: Long, total: Long, status: DownloadStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = :status, errorMessage = :error, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DownloadStatus, error: String? = null, updatedAt: Long = System.currentTimeMillis())
}
