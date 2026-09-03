package io.github.kmbisset89.worldweaver.ui.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun VoiceClipComposeWidget(
    hasClip: Boolean,
    isRecording: Boolean,
    isPlaying: Boolean,
    onAttachSelected: () -> Unit,
    onRecordToggled: () -> Unit,
    onPlayToggled: () -> Unit,
    onRemoveSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Voice clip",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = statusText(hasClip = hasClip, isRecording = isRecording, isPlaying = isPlaying),
                fontSize = 13.sp,
                color = TextSecondary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onAttachSelected,
                    enabled = !isRecording,
                ) {
                    Text("Attach WAV")
                }
                TextButton(onClick = onRecordToggled) {
                    Text(if (isRecording) "Stop recording" else "Record")
                }
                if (hasClip) {
                    TextButton(
                        onClick = onPlayToggled,
                        enabled = !isRecording,
                    ) {
                        Text(if (isPlaying) "Stop" else "Play")
                    }
                    TextButton(
                        onClick = onRemoveSelected,
                        enabled = !isRecording,
                    ) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}

private fun statusText(
    hasClip: Boolean,
    isRecording: Boolean,
    isPlaying: Boolean,
): String {
    return when {
        isRecording -> "Recording… speak the accent or pronunciation, then stop."
        isPlaying -> "Playing the saved clip."
        hasClip -> "Accent or pronunciation clip. Restarting the app keeps this file."
        else -> "Record or attach a WAV clip for accent or pronunciation."
    }
}

internal fun chooseWavPath(title: String): String? {
    val dialog = java.awt.FileDialog(null as java.awt.Frame?, title, java.awt.FileDialog.LOAD)
    dialog.filenameFilter = java.io.FilenameFilter { _, name ->
        name.lowercase().endsWith(".wav")
    }
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return java.io.File(directory, fileName).absolutePath
}
