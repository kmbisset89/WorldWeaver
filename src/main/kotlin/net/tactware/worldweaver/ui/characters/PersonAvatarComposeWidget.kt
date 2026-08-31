package net.tactware.worldweaver.ui.characters

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.TextPrimary
import java.io.File
import javax.imageio.ImageIO

@Composable
internal fun PersonAvatarComposeWidget(
    name: String,
    avatarPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    selected: Boolean = false,
) {
    val bitmap = remember(avatarPath) {
        if (avatarPath.isNullOrBlank()) {
            null
        } else {
            try {
                ImageIO.read(File(avatarPath))?.toComposeImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }
    val border = if (selected) {
        Modifier.border(2.dp, NavyBlue, CircleShape)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size)
            .then(border)
            .clip(CircleShape)
            .background(NavyBlue.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            Text(
                text = initials(name),
                fontSize = (size.value * 0.35f).sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
        }
    }
}

private fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) {
        return "?"
    }
    if (parts.size == 1) {
        return parts[0].take(1).uppercase()
    }
    return (parts[0].take(1) + parts[1].take(1)).uppercase()
}
