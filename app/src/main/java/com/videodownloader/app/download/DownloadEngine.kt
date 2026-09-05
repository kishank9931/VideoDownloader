package com.videodownloader.app.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

sealed class DownloadOutcome {
    data class Success(val totalBytes: Long) : DownloadOutcome()
    data class Paused(val downloadedSoFar: Long) : DownloadOutcome()
    data class Failed(val message: String) : DownloadOutcome()
}

/**
 * Streams a file to disk using an HTTP Range request so a download can be
 * paused (we just stop writing and remember how many bytes we have) and
 * resumed later (we ask the server to continue from that byte offset).
 * Falls back to a full re-download only if the server doesn't support
 * partial content.
 */
class DownloadEngine(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun download(
        url: String,
        destination: File,
        alreadyDownloaded: Long,
        shouldStop: () -> Boolean,
        onProgress: suspend (downloaded: Long, total: Long) -> Unit
    ): DownloadOutcome = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url)
            var resumeFrom = alreadyDownloaded
            if (resumeFrom > 0) {
                requestBuilder.addHeader("Range", "bytes=$resumeFrom-")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext DownloadOutcome.Failed("Server returned ${response.code}")
                }

                val supportsResume = response.code == 206
                if (resumeFrom > 0 && !supportsResume) {
                    // Server ignored the Range request; start over cleanly.
                    resumeFrom = 0
                    destination.delete()
                }

                val contentLength = response.body?.contentLength() ?: -1L
                val total = if (contentLength >= 0) resumeFrom + contentLength else -1L

                RandomAccessFile(destination, "rw").use { raf ->
                    raf.seek(resumeFrom)
                    val input = response.body?.byteStream()
                        ?: return@withContext DownloadOutcome.Failed("Empty response body")

                    val buffer = ByteArray(64 * 1024)
                    var downloaded = resumeFrom
                    var lastReportedAt = 0L

                    while (true) {
                        if (shouldStop()) {
                            return@withContext DownloadOutcome.Paused(downloaded)
                        }
                        val read = input.read(buffer)
                        if (read == -1) break
                        raf.write(buffer, 0, read)
                        downloaded += read

                        val now = System.currentTimeMillis()
                        if (now - lastReportedAt > 200) {
                            onProgress(downloaded, total)
                            lastReportedAt = now
                        }
                    }
                    onProgress(downloaded, total)
                    DownloadOutcome.Success(downloaded)
                }
            }
        } catch (io: java.io.IOException) {
            DownloadOutcome.Failed(io.message ?: "Network error")
        }
    }
}
