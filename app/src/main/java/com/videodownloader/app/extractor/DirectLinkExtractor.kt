package com.videodownloader.app.extractor

import com.videodownloader.app.data.QualityOption
import com.videodownloader.app.data.VideoManifest
import java.net.URLDecoder

/**
 * Handles a URL that already points straight at a video file
 * (http/https ... .mp4/.mkv/.webm/.mov/.m4v/.avi).
 * This covers your own hosted files, CDN links, links you have explicit
 * permission to fetch, and anywhere else a direct file URL is shared.
 */
class DirectLinkExtractor : VideoExtractor {

    private val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".mov", ".m4v", ".avi", ".3gp")

    override fun supports(url: String): Boolean {
        val clean = url.substringBefore('?').lowercase()
        return (clean.startsWith("http://") || clean.startsWith("https://")) &&
            videoExtensions.any { clean.endsWith(it) }
    }

    override suspend fun extract(url: String): VideoManifest {
        val rawName = url.substringBefore('?').substringAfterLast('/')
        val title = runCatching { URLDecoder.decode(rawName, "UTF-8") }.getOrDefault(rawName)
        return VideoManifest(
            title = title.ifBlank { "video" },
            qualities = listOf(QualityOption(label = "Original", url = url))
        )
    }
}
