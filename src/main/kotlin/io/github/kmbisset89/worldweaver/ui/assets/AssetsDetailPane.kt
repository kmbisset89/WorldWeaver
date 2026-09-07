package io.github.kmbisset89.worldweaver.ui.assets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import java.io.File
import javax.imageio.ImageIO

@Composable
internal fun AssetsDetailPane(
    asset: AssetsViewState.AssetItem,
    nameError: String?,
    onInteraction: (AssetsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = asset.displayName,
            onValueChange = { onInteraction(AssetsInteraction.DisplayNameChanged(it)) },
            label = { Text("Name") },
            isError = nameError != null,
            supportingText = nameError?.let { error ->
                { Text(error) }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "${asset.originalFileName} · ${asset.sizeLabel}",
            fontSize = 13.sp,
            color = TextSecondary
        )
        if (asset.isImage) {
            ImagePreview(path = asset.filePath)
        }
        OutlinedTextField(
            value = asset.notes,
            onValueChange = { onInteraction(AssetsInteraction.NotesChanged(it)) },
            label = { Text("Notes") },
            placeholder = { Text("Why you saved this, where it might go…") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onInteraction(AssetsInteraction.OpenFileSelected(asset.id)) }
            ) {
                Text("Open")
            }
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Delete,
                tooltip = "Remove",
                tint = ErrorRed,
                onClick = { onInteraction(AssetsInteraction.DeleteAssetSelected(asset.id)) },
            )
        }
    }
}

@Composable
private fun ImagePreview(path: String?) {
    val bitmap = remember(path) {
        if (path == null) {
            null
        } else {
            runCatching { ImageIO.read(File(path))?.toComposeImageBitmap() }.getOrNull()
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Asset preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .padding(8.dp)
            )
        } else {
            Text(
                text = "Preview unavailable. Open the file instead.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
