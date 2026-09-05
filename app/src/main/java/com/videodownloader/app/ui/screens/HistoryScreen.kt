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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videodownloader.app.MainViewModel
import com.videodownloader.app.data.DownloadStatus
import com.videodownloader.app.util.humanReadableBytes
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val downloads by viewModel.downloads.collectAsState()
    val finished = downloads.filter {
        it.status == DownloadStatus.COMPLETED || it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELLED
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (finished.isEmpty()) {
            Text(
                "Finished downloads will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
            items(finished, key = { it.id }) { item ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(
                                "${item.qualityLabel} • ${humanReadableBytes(item.totalBytes.coerceAtLeast(0))} • " +
                                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(Date(item.updatedAt)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (item.status) {
                                DownloadStatus.COMPLETED -> Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                DownloadStatus.FAILED -> Icon(
                                    Icons.Filled.Error,
                                    contentDescription = "Failed",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                else -> Unit
                            }
                            IconButton(onClick = { viewModel.delete(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
