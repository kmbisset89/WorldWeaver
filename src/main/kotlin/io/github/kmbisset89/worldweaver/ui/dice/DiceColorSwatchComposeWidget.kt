package io.github.kmbisset89.worldweaver.ui.dice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun DiceColorSwatchComposeWidget(
    style: DiceColorStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) NavyBlue else TextSecondary.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(style.body)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = style.displayName },
    )
}
