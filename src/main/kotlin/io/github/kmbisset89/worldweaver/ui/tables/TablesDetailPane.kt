package io.github.kmbisset89.worldweaver.ui.tables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.domain.RandomTableRoll
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun TablesDetailPane(
    table: RandomTable,
    lastRoll: RandomTableRoll?,
    onInteraction: (TablesInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = table.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (table.notes.isNotBlank()) {
            DetailSection("Notes", table.notes)
        }
        RowsSection(table = table)
        if (lastRoll != null) {
            DetailSection("Last roll", lastRoll.displayText())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onInteraction(TablesInteraction.RollSelected(table.id)) }
            ) {
                Text("Roll")
            }
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Edit,
                tooltip = "Edit",
                onClick = { onInteraction(TablesInteraction.EditTableSelected(table.id)) },
            )
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Delete,
                tooltip = "Delete",
                tint = ErrorRed,
                onClick = { onInteraction(TablesInteraction.DeleteTableSelected(table.id)) },
            )
        }
    }
}

@Composable
private fun RowsSection(table: RandomTable) {
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
                text = "Rows",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            val total = table.rows.sumOf { it.weight.coerceAtLeast(0) }
            table.rows.forEach { row ->
                val chance = if (total > 0) {
                    "${row.weight} / $total"
                } else {
                    row.weight.toString()
                }
                Text(
                    text = "${row.label} · $chance",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
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
                color = if (title == "Last roll") NavyBlue else TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
