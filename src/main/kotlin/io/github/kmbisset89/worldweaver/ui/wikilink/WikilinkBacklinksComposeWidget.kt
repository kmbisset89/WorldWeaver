package io.github.kmbisset89.worldweaver.ui.wikilink

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.WikilinkBacklink
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun WikilinkBacklinksComposeWidget(
    backlinks: List<WikilinkBacklink>,
    onBacklinkSelected: (WikilinkBacklink) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Mentioned in",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            if (backlinks.isEmpty()) {
                Text(
                    text = "No notes mention this yet. Type [[ in lore or session notes to link it.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            } else {
                backlinks.forEach { backlink ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { onBacklinkSelected(backlink) },
                    ) {
                        Text(
                            text = "${backlink.sourceKind.label}: ${backlink.sourceTitle}",
                            fontSize = 13.sp,
                            color = NavyBlue,
                        )
                        if (backlink.snippet.isNotBlank()) {
                            Text(
                                text = backlink.snippet,
                                fontSize = 12.sp,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
