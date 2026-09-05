package com.videodownloader.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videodownloader.app.data.QualityOption
import com.videodownloader.app.util.humanReadableBytes

@Composable
fun QualityPickerDialog(
    title: String,
    qualities: List<QualityOption>,
    onConfirm: (QualityOption) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(qualities.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose quality — $title") },
        text = {
            Column {
                qualities.forEach { quality ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = quality == selected,
                                onClick = { selected = quality }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = quality == selected, onClick = { selected = quality })
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(quality.label)
                            quality.sizeBytes?.let { Text(humanReadableBytes(it)) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Download") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
