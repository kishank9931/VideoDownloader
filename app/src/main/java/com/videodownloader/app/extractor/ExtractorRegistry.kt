package com.videodownloader.app.extractor

import com.videodownloader.app.data.VideoManifest

/**
 * Tries each registered extractor in turn and returns the first manifest
 * that resolves. Add new legitimate sources by implementing VideoExtractor
 * and appending an instance to this list.
 */
class ExtractorRegistry(
    private val extractors: List<VideoExtractor> = listOf(
        ManifestExtractor(),
        DirectLinkExtractor()
    )
) {
    suspend fun resolve(url: String): VideoManifest {
        val trimmed = url.trim()
        val extractor = extractors.firstOrNull { it.supports(trimmed) }
            ?: throw UnsupportedSourceException(
                "This link isn't a direct video file or a supported manifest. " +
                    "Downloading from platforms like YouTube or Instagram isn't supported here " +
                    "because they don't offer a public download API and doing so would break " +
                    "their terms of service."
            )
        return extractor.extract(trimmed)
    }
}
