package net.tactware.worldweaver.ui.lore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.Lore
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun LoreDetailPane(
    lore: Lore,
    relatedLinks: List<LoreViewState.RelatedLink>,
    attachedLocationName: String?,
    attachedCharacterName: String?,
    onInteraction: (LoreInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = lore.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = lore.category.displayName,
            fontSize = 13.sp,
            color = TextSecondary
        )
        if (lore.tags.isNotEmpty()) {
            Text(
                text = lore.tags.joinToString(" · "),
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Text(text = lore.content, fontSize = 14.sp, color = TextPrimary)
        if (attachedLocationName != null) {
            DetailSection("Attached location", attachedLocationName)
        }
        if (attachedCharacterName != null) {
            DetailSection("Attached character", attachedCharacterName)
        }
        RelatedSection(relatedLinks = relatedLinks, onInteraction = onInteraction)
        SecretsSection(lore = lore, onInteraction = onInteraction)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onInteraction(LoreInteraction.EditLoreSelected(lore.id)) }) {
                Text("Edit")
            }
            TextButton(onClick = { onInteraction(LoreInteraction.DeleteLoreSelected(lore.id)) }) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun RelatedSection(
    relatedLinks: List<LoreViewState.RelatedLink>,
    onInteraction: (LoreInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Related lore",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (relatedLinks.isEmpty()) {
                Text(
                    text = "No related entries yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                relatedLinks.forEach { link ->
                    Text(
                        text = if (link.missing) "${link.title} (missing)" else link.title,
                        fontSize = 13.sp,
                        color = if (link.missing) TextSecondary else NavyBlue,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .then(
                                if (link.missing) {
                                    Modifier
                                } else {
                                    Modifier.clickable {
                                        onInteraction(LoreInteraction.RelatedLoreSelected(link.loreId))
                                    }
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SecretsSection(
    lore: Lore,
    onInteraction: (LoreInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "DM-only secrets",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Never shown in a player view.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            if (lore.secrets.isEmpty()) {
                Text(
                    text = "No secrets yet.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                lore.secrets.forEach { secret ->
                    Text(
                        text = secret.title.ifBlank { "Untitled secret" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(text = secret.secret, fontSize = 13.sp, color = TextPrimary)
                    secret.hints.forEach { hint ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (hint.revealed) {
                                    "Hint (revealed): ${hint.text}"
                                } else {
                                    "Hint: ${hint.text}"
                                },
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    onInteraction(
                                        LoreInteraction.HintRevealToggled(secret.id, hint.id)
                                    )
                                }
                            ) {
                                Text(if (hint.revealed) "Hide" else "Reveal")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
