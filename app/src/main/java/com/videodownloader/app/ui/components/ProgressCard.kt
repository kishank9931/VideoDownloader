package com.videodownloader.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videodownloader.app.data.DownloadEntity
import com.videodownloader.app.data.DownloadStatus
import com.videodownloader.app.util.humanReadableBytes

@Composable
fun ProgressCard(
    item: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        "${item.qualityLabel} • ${statusLabel(item.status)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    if (item.status == DownloadStatus.RUNNING || item.status == DownloadStatus.QUEUED) {
                        IconButton(onClick = onPause) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pause")
                        }
                    } else if (item.status == DownloadStatus.PAUSED || item.status == DownloadStatus.FAILED) {
                        IconButton(onClick = onResume) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
                        }
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                }
            }

            if (item.status == DownloadStatus.RUNNING || item.status == DownloadStatus.PAUSED || item.status == DownloadStatus.QUEUED) {
                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
                Text(
                    "${humanReadableBytes(item.downloadedBytes)} of ${
                        if (item.totalBytes > 0) humanReadableBytes(item.totalBytes) else "?"
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (item.status == DownloadStatus.FAILED && item.errorMessage != null) {
                Text(
                    item.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun statusLabel(status: DownloadStatus): String = when (status) {
    DownloadStatus.QUEUED -> "Queued"
    DownloadStatus.RUNNING -> "Downloading"
    DownloadStatus.PAUSED -> "Paused"
    DownloadStatus.COMPLETED -> "Completed"
    DownloadStatus.FAILED -> "Failed"
    DownloadStatus.CANCELLED -> "Cancelled"
}
