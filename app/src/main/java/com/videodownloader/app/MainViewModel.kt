package com.videodownloader.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.videodownloader.app.data.DownloadEntity
import com.videodownloader.app.data.DownloadRepository
import com.videodownloader.app.data.QualityOption
import com.videodownloader.app.data.VideoManifest
import com.videodownloader.app.extractor.ExtractorRegistry
import com.videodownloader.app.extractor.UnsupportedSourceException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ResolveState {
    object Idle : ResolveState()
    object Loading : ResolveState()
    data class NeedsQualityPick(val sourceUrl: String, val manifest: VideoManifest) : ResolveState()
    data class Error(val message: String) : ResolveState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DownloadRepository(application)
    private val registry = ExtractorRegistry()

    val downloads: StateFlow<List<DownloadEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _resolveState = MutableStateFlow<ResolveState>(ResolveState.Idle)
    val resolveState: StateFlow<ResolveState> = _resolveState

    fun onUrlSubmitted(url: String) {
        if (url.isBlank()) return
        _resolveState.value = ResolveState.Loading
        viewModelScope.launch {
            try {
                val manifest = registry.resolve(url)
                if (manifest.qualities.size == 1) {
                    startDownload(url, manifest, manifest.qualities.first())
                } else {
                    _resolveState.value = ResolveState.NeedsQualityPick(url, manifest)
                }
            } catch (e: UnsupportedSourceException) {
                _resolveState.value = ResolveState.Error(e.message ?: "Unsupported link")
            } catch (e: Exception) {
                _resolveState.value = ResolveState.Error(e.message ?: "Couldn't read that link")
            }
        }
    }

    fun onQualityChosen(sourceUrl: String, manifest: VideoManifest, quality: QualityOption) {
        startDownload(sourceUrl, manifest, quality)
    }

    fun dismissResolveState() {
        _resolveState.value = ResolveState.Idle
    }

    private fun startDownload(sourceUrl: String, manifest: VideoManifest, quality: QualityOption) {
        viewModelScope.launch {
            repository.startNew(
                title = manifest.title,
                sourceUrl = sourceUrl,
                downloadUrl = quality.url,
                qualityLabel = quality.label
            )
            _resolveState.value = ResolveState.Idle
        }
    }

    fun pause(id: Long) = viewModelScope.launch { repository.pause(id) }
    fun resume(id: Long) = viewModelScope.launch { repository.resume(id) }
    fun cancel(id: Long) = viewModelScope.launch { repository.cancel(id) }
    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }
}
