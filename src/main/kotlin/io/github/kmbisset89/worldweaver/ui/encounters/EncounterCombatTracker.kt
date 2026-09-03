package io.github.kmbisset89.worldweaver.ui.encounters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.CombatState
import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.EncounterParticipant
import io.github.kmbisset89.worldweaver.domain.EncounterParticipantSource
import io.github.kmbisset89.worldweaver.domain.FifthEditionCondition
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun EncounterCombatTracker(
    encounterId: String,
    initiativeOrder: List<EncounterParticipant>,
    currentTurnParticipantId: String?,
    selectedParticipantId: String?,
    combatAmount: String,
    availableConditions: List<FifthEditionCondition>,
    deathSaves: DeathSaves?,
    onInteraction: (EncountersInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(initiativeOrder, key = { it.id }) { participant ->
            CombatantRow(
                encounterId = encounterId,
                participant = participant,
                isCurrentTurn = participant.id == currentTurnParticipantId,
                isSelected = participant.id == selectedParticipantId,
                combatAmount = combatAmount,
                availableConditions = availableConditions,
                deathSaves = if (participant.id == selectedParticipantId) deathSaves else null,
                onInteraction = onInteraction,
            )
        }
    }
}

@Composable
private fun CombatantRow(
    encounterId: String,
    participant: EncounterParticipant,
    isCurrentTurn: Boolean,
    isSelected: Boolean,
    combatAmount: String,
    availableConditions: List<FifthEditionCondition>,
    deathSaves: DeathSaves?,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    val groupLabel = if (participant.groupCount > 1) " ×${participant.groupCount}" else ""
    val hpFraction = if (participant.maxHitPoints <= 0) {
        0f
    } else {
        participant.hitPoints.toFloat() / participant.maxHitPoints.toFloat()
    }
    val bloodied = hpFraction <= 0.5f && participant.hitPoints > 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    isCurrentTurn -> NavyBlue.copy(alpha = 0.12f)
                    isSelected -> NavyBlue.copy(alpha = 0.06f)
                    else -> SurfaceCard
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                onInteraction(EncountersInteraction.ParticipantSelected(participant.id))
            }
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${participant.name}$groupLabel",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = "AC ${participant.armorClass} · HP ${participant.hitPoints}/${participant.maxHitPoints}" +
                if (participant.temporaryHitPoints > 0) {
                    " (temp ${participant.temporaryHitPoints})"
                } else {
                    ""
                },
            fontSize = 12.sp,
            color = if (bloodied || participant.combatState != CombatState.Conscious) {
                ErrorRed
            } else {
                TextSecondary
            }
        )
        val barProgress = hpFraction.coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { barProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = if (bloodied) ErrorRed else NavyBlue,
            trackColor = TextSecondary.copy(alpha = 0.2f),
        )
        ActionEconomySummary(
            participant = participant,
            onSelect = {
                onInteraction(EncountersInteraction.ParticipantSelected(participant.id))
            },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterChip(
                selected = participant.visibleToPlayers,
                onClick = {
                    onInteraction(
                        EncountersInteraction.PlayerVisibilityToggled(
                            encounterId,
                            participant.id,
                            !participant.visibleToPlayers,
                        )
                    )
                },
                label = { Text(if (participant.visibleToPlayers) "Visible" else "Hidden") },
            )
            if (participant.combatState != CombatState.Conscious) {
                Text(
                    text = participant.combatState.displayName,
                    fontSize = 12.sp,
                    color = ErrorRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        if (participant.conditions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                participant.conditions.forEach { condition ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            onInteraction(
                                EncountersInteraction.ConditionRemoved(
                                    encounterId,
                                    participant.id,
                                    condition,
                                )
                            )
                        },
                        label = { Text(condition) },
                    )
                }
            }
        }
        if (isSelected) {
            ActionEconomyPad(
                encounterId = encounterId,
                participant = participant,
                onInteraction = onInteraction,
            )
            HitPointPad(
                encounterId = encounterId,
                participantId = participant.id,
                combatAmount = combatAmount,
                onInteraction = onInteraction,
            )
            ConditionPicker(
                encounterId = encounterId,
                participantId = participant.id,
                availableConditions = availableConditions,
                applied = participant.conditions,
                onInteraction = onInteraction,
            )
            if (deathSaves != null && participant.combatState == CombatState.Downed) {
                DeathSavePad(
                    participantId = participant.id,
                    deathSaves = deathSaves,
                    onInteraction = onInteraction,
                )
            }
            if (participant.source != EncounterParticipantSource.Nameless &&
                participant.sourceId != null
            ) {
                TextButton(
                    onClick = {
                        onInteraction(
                            EncountersInteraction.SheetSelected(
                                source = participant.source,
                                sourceId = participant.sourceId,
                            )
                        )
                    }
                ) {
                    Text("Sheet")
                }
            }
        }
    }
}

@Composable
private fun ActionEconomySummary(
    participant: EncounterParticipant,
    onSelect: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilterChip(
            selected = participant.attacksUsed > 0,
            onClick = onSelect,
            label = { Text("Atk ${participant.attacksUsed}/${participant.attacksAllowed}") },
        )
        FilterChip(
            selected = participant.bonusActionUsed,
            onClick = onSelect,
            label = { Text("BA") },
        )
        FilterChip(
            selected = participant.reactionUsed,
            onClick = onSelect,
            label = { Text("R") },
        )
    }
}

@Composable
private fun ActionEconomyPad(
    encounterId: String,
    participant: EncounterParticipant,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    val allowedChoices = buildList {
        addAll(1..EncounterParticipant.ATTACKS_ALLOWED_CHIP_MAX)
        if (participant.attacksAllowed !in 1..EncounterParticipant.ATTACKS_ALLOWED_CHIP_MAX) {
            add(participant.attacksAllowed)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Attacks used", fontSize = 12.sp, color = TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            (0..participant.attacksAllowed).forEach { count ->
                FilterChip(
                    selected = participant.attacksUsed == count,
                    onClick = {
                        onInteraction(
                            EncountersInteraction.AttacksUsedSelected(
                                encounterId,
                                participant.id,
                                count,
                            )
                        )
                    },
                    label = { Text(count.toString()) },
                )
            }
        }
        Text("Attacks allowed", fontSize = 12.sp, color = TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            allowedChoices.forEach { count ->
                FilterChip(
                    selected = participant.attacksAllowed == count,
                    onClick = {
                        onInteraction(
                            EncountersInteraction.AttacksAllowedSelected(
                                encounterId,
                                participant.id,
                                count,
                            )
                        )
                    },
                    label = { Text(count.toString()) },
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterChip(
                selected = participant.bonusActionUsed,
                onClick = {
                    onInteraction(
                        EncountersInteraction.BonusActionSet(
                            encounterId,
                            participant.id,
                            !participant.bonusActionUsed,
                        )
                    )
                },
                label = { Text("Bonus") },
            )
            FilterChip(
                selected = participant.reactionUsed,
                onClick = {
                    onInteraction(
                        EncountersInteraction.ReactionSet(
                            encounterId,
                            participant.id,
                            !participant.reactionUsed,
                        )
                    )
                },
                label = { Text("Reaction") },
            )
        }
    }
}

@Composable
private fun HitPointPad(
    encounterId: String,
    participantId: String,
    combatAmount: String,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = combatAmount,
            onValueChange = { onInteraction(EncountersInteraction.CombatAmountChanged(it)) },
            label = { Text("Amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = {
                    onInteraction(EncountersInteraction.DamageApplied(encounterId, participantId))
                }
            ) {
                Text("Damage")
            }
            TextButton(
                onClick = {
                    onInteraction(EncountersInteraction.HealApplied(encounterId, participantId))
                }
            ) {
                Text("Heal")
            }
            TextButton(
                onClick = {
                    onInteraction(
                        EncountersInteraction.TemporaryHitPointsSet(encounterId, participantId)
                    )
                }
            ) {
                Text("Temp")
            }
        }
    }
}

@Composable
private fun ConditionPicker(
    encounterId: String,
    participantId: String,
    availableConditions: List<FifthEditionCondition>,
    applied: List<String>,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Conditions", fontSize = 12.sp, color = TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            availableConditions.forEach { condition ->
                val selected = applied.any { it.equals(condition.displayName, ignoreCase = true) }
                FilterChip(
                    selected = selected,
                    onClick = {
                        onInteraction(
                            EncountersInteraction.ConditionToggled(
                                encounterId,
                                participantId,
                                condition,
                            )
                        )
                    },
                    label = { Text(condition.displayName) },
                )
            }
        }
    }
}

@Composable
private fun DeathSavePad(
    participantId: String,
    deathSaves: DeathSaves,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Death saves", fontSize = 12.sp, color = TextSecondary)
        Text("Successes", fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0..DeathSaves.LIMIT).forEach { count ->
                FilterChip(
                    selected = deathSaves.successes == count,
                    onClick = {
                        onInteraction(
                            EncountersInteraction.DeathSaveSuccessSelected(participantId, count)
                        )
                    },
                    label = { Text(count.toString()) },
                )
            }
        }
        Text("Failures", fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0..DeathSaves.LIMIT).forEach { count ->
                FilterChip(
                    selected = deathSaves.failures == count,
                    onClick = {
                        onInteraction(
                            EncountersInteraction.DeathSaveFailureSelected(participantId, count)
                        )
                    },
                    label = { Text(count.toString()) },
                )
            }
        }
    }
}
