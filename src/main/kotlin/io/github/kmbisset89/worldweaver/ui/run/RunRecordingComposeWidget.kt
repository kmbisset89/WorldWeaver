package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.SessionMicrophoneDevice
import io.github.kmbisset89.worldweaver.domain.SessionRecordingKind
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun RunRecordingComposeWidget(
    hasMicrophone: Boolean,
    hasCamera: Boolean,
    microphones: List<SessionMicrophoneDevice>,
    selectedMicrophoneId: String?,
    recordingMode: SessionRecordingKind,
    isRecording: Boolean,
    elapsedLabel: String,
    recordings: List<RunViewState.RecordingLine>,
    recordingError: String?,
    cameraNeedsPermission: Boolean,
    cameraPreview: StateFlow<ImageBitmap?>,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preview by cameraPreview.collectAsState()
    RunCardComposeWidget(title = "Record", modifier = modifier) {
        Text(
            text = statusText(
                hasMicrophone = hasMicrophone,
                hasCamera = hasCamera,
                recordingMode = recordingMode,
                isRecording = isRecording,
                elapsedLabel = elapsedLabel,
                recordingError = recordingError,
                cameraNeedsPermission = cameraNeedsPermission,
            ),
            fontSize = 13.sp,
            color = if (recordingError != null) ErrorRed else TextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = recordingMode == SessionRecordingKind.Audio,
                onClick = { onInteraction(RunInteraction.RecordingModeSelected(SessionRecordingKind.Audio)) },
                enabled = !isRecording && hasMicrophone,
                label = { Text("Mic") },
            )
            FilterChip(
                selected = recordingMode == SessionRecordingKind.Video,
                onClick = { onInteraction(RunInteraction.RecordingModeSelected(SessionRecordingKind.Video)) },
                enabled = !isRecording && (hasCamera || cameraNeedsPermission),
                label = { Text("Camera") },
            )
        }
        if (microphones.isNotEmpty()) {
            MicrophoneDropdown(
                microphones = microphones,
                selectedMicrophoneId = selectedMicrophoneId,
                enabled = !isRecording,
                onInteraction = onInteraction,
            )
        }
        if (recordingMode == SessionRecordingKind.Video) {
            CameraPreviewFrame(preview = preview, cameraNeedsPermission = cameraNeedsPermission)
            if (cameraNeedsPermission && !isRecording) {
                OutlinedButton(
                    onClick = { onInteraction(RunInteraction.CameraPermissionRequested) },
                ) {
                    Text("Allow camera")
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isRecording) {
                OutlinedButton(onClick = { onInteraction(RunInteraction.RecordToggled) }) {
                    Text("Stop")
                }
                Text(
                    text = elapsedLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            } else {
                val canRecord = when (recordingMode) {
                    SessionRecordingKind.Audio -> hasMicrophone
                    SessionRecordingKind.Video -> hasCamera || cameraNeedsPermission
                }
                Button(
                    onClick = { onInteraction(RunInteraction.RecordToggled) },
                    enabled = canRecord,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                ) {
                    Text("Record")
                }
            }
        }
        if (recordings.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recordings.forEach { recording ->
                    RecordingRow(
                        recording = recording,
                        enabled = !isRecording,
                        onInteraction = onInteraction,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MicrophoneDropdown(
    microphones: List<SessionMicrophoneDevice>,
    selectedMicrophoneId: String?,
    enabled: Boolean,
    onInteraction: (RunInteraction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = microphones.firstOrNull { device -> device.id == selectedMicrophoneId }?.label
        ?: microphones.first().label
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { next ->
            if (enabled) {
                expanded = next
            }
        },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Microphone") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            microphones.forEach { device ->
                DropdownMenuItem(
                    text = { Text(device.label) },
                    onClick = {
                        onInteraction(RunInteraction.MicrophoneSelected(device.id))
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewFrame(
    preview: ImageBitmap?,
    cameraNeedsPermission: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard),
        contentAlignment = Alignment.Center,
    ) {
        if (preview != null) {
            Image(
                bitmap = preview,
                contentDescription = "Camera preview",
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = if (cameraNeedsPermission) {
                    "Camera access is needed for preview."
                } else {
                    "Starting camera…"
                },
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun RecordingRow(
    recording: RunViewState.RecordingLine,
    enabled: Boolean,
    onInteraction: (RunInteraction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${recording.kindLabel} · ${recording.startedLabel} · ${recording.sizeLabel}",
            fontSize = 13.sp,
            color = TextPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onInteraction(RunInteraction.RecordingOpened(recording.path)) },
                enabled = enabled,
            ) {
                Text("Open")
            }
            TextButton(
                onClick = { onInteraction(RunInteraction.RecordingDeleteSelected(recording.id)) },
                enabled = enabled,
            ) {
                Text("Delete")
            }
        }
    }
}

private fun statusText(
    hasMicrophone: Boolean,
    hasCamera: Boolean,
    recordingMode: SessionRecordingKind,
    isRecording: Boolean,
    elapsedLabel: String,
    recordingError: String?,
    cameraNeedsPermission: Boolean,
): String {
    if (recordingError != null) {
        return recordingError
    }
    if (isRecording) {
        return "Recording… $elapsedLabel"
    }
    if (recordingMode == SessionRecordingKind.Audio && !hasMicrophone) {
        return "No microphone available. Allow microphone access or plug one in."
    }
    if (recordingMode == SessionRecordingKind.Video && cameraNeedsPermission) {
        return "Camera access is needed. Tap Allow camera."
    }
    if (recordingMode == SessionRecordingKind.Video && !hasCamera) {
        return "No camera available. Allow camera access or plug one in."
    }
    return "Record the table. Choose a microphone. Camera mode previews the selected camera. Files stay on this computer with the session."
}
