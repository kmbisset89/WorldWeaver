package io.github.kmbisset89.worldweaver.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActionIconButtonComposeWidget(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
    tint: Color = Color.Unspecified,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        if (filled) {
            FilledIconButton(
                onClick = onClick,
                enabled = enabled,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = NavyBlue,
                    contentColor = contentColorFor(NavyBlue),
                ),
            ) {
                ActionIcon(
                    icon = icon,
                    tooltip = tooltip,
                    tint = tint,
                )
            }
        } else {
            IconButton(
                onClick = onClick,
                enabled = enabled,
            ) {
                ActionIcon(
                    icon = icon,
                    tooltip = tooltip,
                    tint = tint,
                )
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: ImageVector,
    tooltip: String,
    tint: Color,
) {
    Icon(
        imageVector = icon,
        contentDescription = tooltip,
        tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
    )
}
