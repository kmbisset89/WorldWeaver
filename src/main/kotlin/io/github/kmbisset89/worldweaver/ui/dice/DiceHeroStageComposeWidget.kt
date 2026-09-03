package io.github.kmbisset89.worldweaver.ui.dice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.DiceRollResult
import io.github.kmbisset89.worldweaver.domain.DiceRollSource
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun DiceHeroStageComposeWidget(
    state: DiceViewState.Content,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val result = state.lastResult
        if (result == null) {
            ReadyStage(state = state)
        } else {
            ResultStage(
                result = result,
                colorStyle = state.colorStyle,
                rollToken = state.rollToken,
            )
        }
    }
}

@Composable
private fun ReadyStage(
    state: DiceViewState.Content,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 188.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        DieFaceComposeWidget(
            die = state.selectedDie,
            colorStyle = state.colorStyle,
            size = 96.dp,
        )
        Text(
            text = "Ready to roll",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Text(
            text = state.notationText.ifBlank { state.selectedDie.label },
            fontSize = 14.sp,
            color = TextSecondary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultStage(
    result: DiceRollResult,
    colorStyle: DiceColorStyle,
    rollToken: Long,
) {
    val die = DieSides.fromSides(result.sides) ?: DieSides.D20
    val animate = result.source == DiceRollSource.Automated
    val dieSize = when {
        result.faces.size == 1 -> 96.dp
        result.faces.size > 4 -> 56.dp
        else -> 80.dp
    }
    val naturalTwenty = isNaturalTwenty(result)
    val naturalOne = isNaturalOne(result)
    val wash = when {
        naturalTwenty -> NaturalTwentyGold.copy(alpha = 0.12f)
        naturalOne -> NaturalOneEmber.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val headline = when {
        naturalTwenty -> "Natural 20!"
        naturalOne -> "Natural 1"
        isTableRoll(result) -> "Table roll"
        else -> "Last roll"
    }
    val headlineColor = when {
        naturalTwenty -> NaturalTwentyGold
        naturalOne -> NaturalOneEmber
        else -> TextSecondary
    }
    val totalColor = when {
        naturalTwenty -> NaturalTwentyGold
        naturalOne -> NaturalOneEmber
        else -> TextPrimary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(wash, RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 188.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = headline,
                fontSize = if (naturalTwenty || naturalOne) 16.sp else 13.sp,
                fontWeight = if (naturalTwenty || naturalOne) FontWeight.Bold else FontWeight.Normal,
                color = headlineColor,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                result.faces.forEachIndexed { index, face ->
                    DieFaceComposeWidget(
                        die = die,
                        colorStyle = colorStyle,
                        face = face,
                        size = dieSize,
                        dimmed = isDiscardedFace(result, face),
                        rollToken = rollToken,
                        staggerIndex = index,
                        animate = animate,
                    )
                }
            }
            Text(
                text = result.total.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = totalColor,
            )
            Text(
                text = "${diceRollNotation(result)} · ${formatDiceRollFaces(result)}",
                fontSize = 14.sp,
                color = TextSecondary,
            )
        }
    }
}
