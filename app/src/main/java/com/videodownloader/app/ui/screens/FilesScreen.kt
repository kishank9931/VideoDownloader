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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videodownloader.app.data.DownloadRepository
import com.videodownloader.app.util.humanReadableBytes
import com.videodownloader.app.util.openIntentFor
import com.videodownloader.app.util.shareIntentFor
import java.io.File

@Composable
fun FilesScreen() {
    val context = LocalContext.current
    val dir = remember { DownloadRepository.downloadsDir(context) }
    var files by remember {
        mutableStateOf(dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList())
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Files", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (files.isEmpty()) {
            Text(
                "Downloaded videos will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)) {
            items(files, key = { it.absolutePath }) { file ->
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
                            Text(file.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(
                                humanReadableBytes(file.length()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            IconButton(onClick = { context.startActivity(openIntentFor(context, file)) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                            }
                            IconButton(onClick = { context.startActivity(shareIntentFor(context, file)) }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share")
                            }
                            IconButton(onClick = {
                                file.delete()
                                files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
