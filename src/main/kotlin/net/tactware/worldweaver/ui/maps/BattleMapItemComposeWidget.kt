package net.tactware.worldweaver.ui.maps

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.TextPrimary

@Composable
internal fun BattleMapItemComposeWidget(
    name: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val pieceShape = RoundedCornerShape(4.dp)
    val highlight = if (selected) {
        Modifier.border(2.dp, NavyBlue, pieceShape)
    } else {
        Modifier
    }
    Column(
        modifier = modifier.width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .then(highlight)
                .background(ItemFill, pieceShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials(name),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Text(
            text = name,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
        )
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

private val ItemFill = Color(0xE0B45309)
