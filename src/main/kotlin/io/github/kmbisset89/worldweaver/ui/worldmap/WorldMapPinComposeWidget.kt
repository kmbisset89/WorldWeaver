package io.github.kmbisset89.worldweaver.ui.worldmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary

@Composable
internal fun WorldMapPinComposeWidget(
    name: String,
    hasMap: Boolean,
    selected: Boolean,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NavyBlue else SurfaceCard)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (hasMap) "$name ▸" else name,
            fontSize = 12.sp,
            color = if (selected) SurfaceCard else TextPrimary,
        )
    }
}
