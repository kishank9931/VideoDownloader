package com.videodownloader.app.extractor

import com.videodownloader.app.data.QualityOption
import com.videodownloader.app.data.VideoManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Handles a small JSON manifest format for sources that want to offer
 * multiple qualities, e.g.:
 *
 * {
 *   "title": "My Clip",
 *   "qualities": [
 *     { "label": "1080p", "url": "https://cdn.example.com/clip-1080.mp4", "sizeBytes": 104857600 },
 *     { "label": "720p",  "url": "https://cdn.example.com/clip-720.mp4"  }
 *   ]
 * }
 *
 * Any server you control (or any partner who publishes this format) can be
 * supported with zero code changes — just link to the .json manifest.
 */
class ManifestExtractor(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : VideoExtractor {

    override fun supports(url: String): Boolean {
        val clean = url.substringBefore('?').lowercase()
        return (clean.startsWith("http://") || clean.startsWith("https://")) && clean.endsWith(".json")
    }

    override suspend fun extract(url: String): VideoManifest = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw UnsupportedSourceException("Manifest fetch failed (${response.code})")
            }
            val body = response.body?.string() ?: throw UnsupportedSourceException("Empty manifest")
            val json = JSONObject(body)
            val title = json.optString("title", "video")
            val qualitiesJson = json.getJSONArray("qualities")
            val qualities = buildList {
                for (i in 0 until qualitiesJson.length()) {
                    val q = qualitiesJson.getJSONObject(i)
                    add(
                        QualityOption(
                            label = q.getString("label"),
                            url = q.getString("url"),
                            sizeBytes = if (q.has("sizeBytes")) q.getLong("sizeBytes") else null
                        )
                    )
                }
            }
            if (qualities.isEmpty()) throw UnsupportedSourceException("Manifest has no qualities")
            VideoManifest(title = title, qualities = qualities)
        }
    }
}
