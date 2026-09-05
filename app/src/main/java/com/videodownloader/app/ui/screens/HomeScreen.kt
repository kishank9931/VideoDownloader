package com.videodownloader.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videodownloader.app.MainViewModel
import com.videodownloader.app.ResolveState
import com.videodownloader.app.data.DownloadStatus
import com.videodownloader.app.ui.components.ProgressCard
import com.videodownloader.app.ui.components.QualityPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var url by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val downloads by viewModel.downloads.collectAsState()
    val resolveState by viewModel.resolveState.collectAsState()
    val active = downloads.filter {
        it.status == DownloadStatus.RUNNING || it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.PAUSED
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Video Downloader", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Paste a direct video link or a quality manifest URL",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("https://example.com/video.mp4") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.getText()?.text?.let { url = it }
                    }) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Paste")
                    }
                }
            )
        }

        Button(
            onClick = { viewModel.onUrlSubmitted(url) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = url.isNotBlank() && resolveState != ResolveState.Loading
        ) {
            if (resolveState == ResolveState.Loading) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            } else {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            }
            Text("Download")
        }

        when (val state = resolveState) {
            is ResolveState.Error -> Text(
                state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            is ResolveState.NeedsQualityPick -> QualityPickerDialog(
                title = state.manifest.title,
                qualities = state.manifest.qualities,
                onConfirm = { quality -> viewModel.onQualityChosen(state.sourceUrl, state.manifest, quality) },
                onDismiss = { viewModel.dismissResolveState() }
            )
            else -> Unit
        }

        Text(
            "Active downloads",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        if (active.isEmpty()) {
            Text(
                "Nothing downloading right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(active, key = { it.id }) { item ->
                    ProgressCard(
                        item = item,
                        onPause = { viewModel.pause(item.id) },
                        onResume = { viewModel.resume(item.id) },
                        onCancel = { viewModel.cancel(item.id) }
                    )
                }
            }
        }
    }
}
