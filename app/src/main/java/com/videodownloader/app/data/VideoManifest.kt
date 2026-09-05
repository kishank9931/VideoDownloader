package com.videodownloader.app.data

/**
 * A single downloadable rendition of a video (e.g. "1080p", "720p").
 */
data class QualityOption(
    val label: String,
    val url: String,
    val sizeBytes: Long? = null
)

/**
 * The result of resolving a pasted link: a title plus one or more quality
 * options the user can pick between. A plain direct link to an .mp4 resolves
 * to a single-quality manifest; a JSON manifest URL can describe several.
 */
data class VideoManifest(
    val title: String,
    val qualities: List<QualityOption>
)
