package net.tactware.worldweaver.ui.maps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.CombatState
import net.tactware.worldweaver.ui.theme.ErrorRed
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SuccessGreen
import net.tactware.worldweaver.ui.theme.TextPrimary
import java.io.File
import javax.imageio.ImageIO

@Composable
internal fun BattleMapTokenComposeWidget(
    name: String,
    avatarPath: String?,
    combatState: CombatState,
    conditions: List<String>,
    selected: Boolean,
    hiddenFromPlayers: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
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
    val pieceShape = RoundedCornerShape(6.dp)
    val highlight = if (selected) {
        Modifier.border(2.dp, NavyBlue, pieceShape)
    } else {
        Modifier
    }
    val stateColor = when (combatState) {
        CombatState.Conscious -> SuccessGreen
        CombatState.Downed, CombatState.Dead -> ErrorRed
    }
    val shownConditions = conditions.take(2)
    val extraConditions = conditions.size - shownConditions.size
    Column(
        modifier = modifier
            .width(size)
            .then(if (hiddenFromPlayers) Modifier.alpha(0.45f) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .then(highlight)
                .clip(pieceShape)
                .background(NavyBlue.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size).clip(pieceShape),
                )
            } else {
                Text(
                    text = initials(name),
                    fontSize = (size.value * 0.32f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
            if (hiddenFromPlayers) {
                Text(
                    text = "Hidden",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color(0xCC1F2937))
                        .padding(vertical = 1.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        )
        Text(
            text = combatState.displayName,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            color = stateColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        shownConditions.forEach { condition ->
            Text(
                text = condition,
                fontSize = 8.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (extraConditions > 0) {
            Text(
                text = "+$extraConditions",
                fontSize = 8.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
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
