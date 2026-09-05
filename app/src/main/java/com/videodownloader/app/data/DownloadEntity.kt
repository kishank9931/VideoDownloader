package com.videodownloader.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per download: the source link, the chosen quality, where the file
 * lives on disk, and how far it has progressed. This table is what powers
 * both the live progress list and the History screen.
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceUrl: String,
    val downloadUrl: String,
    val qualityLabel: String,
    val fileName: String,
    val filePath: String,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}
