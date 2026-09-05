package com.videodownloader.app.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.videodownloader.app.data.AppDatabase
import com.videodownloader.app.data.DownloadStatus
import java.io.File

/**
 * One WorkManager job = one active download. Pausing a download simply
 * cancels this worker (see DownloadRepository); the partial file and the
 * byte count already written to Room let a fresh worker pick up where it
 * left off.
 */
class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val downloadId = inputData.getLong(KEY_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return Result.failure()

        val dao = AppDatabase.get(applicationContext).downloadDao()
        val entity = dao.getById(downloadId) ?: return Result.failure()
        val destination = File(entity.filePath)
        destination.parentFile?.mkdirs()

        dao.updateStatus(downloadId, DownloadStatus.RUNNING)

        val engine = DownloadEngine()
        val outcome = engine.download(
            url = entity.downloadUrl,
            destination = destination,
            alreadyDownloaded = entity.downloadedBytes,
            shouldStop = { isStopped },
            onProgress = { downloaded, total ->
                dao.updateProgress(downloadId, downloaded, total, DownloadStatus.RUNNING)
                setProgress(
                    androidx.work.Data.Builder()
                        .putLong(KEY_DOWNLOADED, downloaded)
                        .putLong(KEY_TOTAL, total)
                        .build()
                )
            }
        )

        return when (outcome) {
            is DownloadOutcome.Success -> {
                dao.updateProgress(downloadId, outcome.totalBytes, outcome.totalBytes, DownloadStatus.COMPLETED)
                Result.success()
            }
            is DownloadOutcome.Paused -> {
                dao.updateProgress(downloadId, outcome.downloadedSoFar, entity.totalBytes, DownloadStatus.PAUSED)
                Result.success()
            }
            is DownloadOutcome.Failed -> {
                dao.updateStatus(downloadId, DownloadStatus.FAILED, outcome.message)
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_DOWNLOADED = "downloaded"
        const val KEY_TOTAL = "total"
        fun uniqueWorkName(downloadId: Long) = "download_$downloadId"
    }
}
