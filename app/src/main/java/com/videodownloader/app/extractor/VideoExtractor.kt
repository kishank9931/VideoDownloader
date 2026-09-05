package com.videodownloader.app.extractor

import com.videodownloader.app.data.VideoManifest

/**
 * A source resolver: turns a pasted URL into one or more downloadable
 * quality options.
 *
 * IMPORTANT — by design this app only ships extractors for sources that are
 * legitimately accessible: direct file links, and a simple JSON manifest
 * format for services that choose to publish one. It deliberately does NOT
 * include scrapers/extractors for YouTube, Instagram, TikTok, or similar
 * platforms, because none of them offer a public API for downloading
 * arbitrary video content, and reverse-engineering their private endpoints
 * to do so breaks their Terms of Service and can infringe creators'
 * copyright. If you have a licensed API key or partner agreement with a
 * platform, add a new Extractor implementation for it here and register it
 * in ExtractorRegistry — the rest of the app (progress, pause/resume,
 * history, file manager) needs no changes.
 */
interface VideoExtractor {
    fun supports(url: String): Boolean
    suspend fun extract(url: String): VideoManifest
}

class UnsupportedSourceException(message: String) : Exception(message)
