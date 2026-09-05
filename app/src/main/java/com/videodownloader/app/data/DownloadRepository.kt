package com.videodownloader.app.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.videodownloader.app.download.DownloadWorker
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Single entry point the UI talks to: start/pause/resume/cancel/delete a
 * download, and observe the live list for the Home + History screens.
 */
class DownloadRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = AppDatabase.get(appContext).downloadDao()
    private val workManager = WorkManager.getInstance(appContext)

    fun observeAll(): Flow<List<DownloadEntity>> = dao.observeAll()

    suspend fun startNew(title: String, sourceUrl: String, downloadUrl: String, qualityLabel: String): Long {
        val safeName = sanitizeFileName(title) + guessExtension(downloadUrl)
        val dir = downloadsDir(appContext)
        dir.mkdirs()
        val destination = File(dir, safeName)

        val id = dao.insert(
            DownloadEntity(
                title = title,
                sourceUrl = sourceUrl,
                downloadUrl = downloadUrl,
                qualityLabel = qualityLabel,
                fileName = safeName,
                filePath = destination.absolutePath,
                status = DownloadStatus.QUEUED
            )
        )
        enqueue(id)
        return id
    }

    suspend fun pause(id: Long) {
        workManager.cancelUniqueWork(DownloadWorker.uniqueWorkName(id))
        dao.updateStatus(id, DownloadStatus.PAUSED)
    }

    suspend fun resume(id: Long) {
        enqueue(id)
    }

    suspend fun cancel(id: Long) {
        workManager.cancelUniqueWork(DownloadWorker.uniqueWorkName(id))
        val entity = dao.getById(id) ?: return
        File(entity.filePath).delete()
        dao.updateStatus(id, DownloadStatus.CANCELLED)
    }

    suspend fun delete(id: Long) {
        val entity = dao.getById(id) ?: return
        File(entity.filePath).delete()
        dao.deleteById(id)
    }

    private fun enqueue(id: Long) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.KEY_DOWNLOAD_ID to id))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.enqueueUniqueWork(
            DownloadWorker.uniqueWorkName(id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun sanitizeFileName(name: String): String =
        name.trim().replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifBlank { "video_${System.currentTimeMillis()}" }

    private fun guessExtension(url: String): String {
        val clean = url.substringBefore('?').lowercase()
        val known = listOf(".mp4", ".mkv", ".webm", ".mov", ".m4v", ".avi", ".3gp")
        val existing = known.firstOrNull { clean.endsWith(it) }
        return existing ?: ".mp4"
    }

    companion object {
        /**
         * Must stay in sync with res/xml/file_paths.xml (the "downloads"
         * external-files-path entry) so FileProvider can grant URIs for
         * these files to Files/Share screens.
         */
        fun downloadsDir(context: Context): File =
            File(context.getExternalFilesDir(null), "VideoDownloader")
    }
}
