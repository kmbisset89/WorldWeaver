package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun RunTablesComposeWidget(
    tables: List<RunViewState.TableLine>,
    lastRoll: String?,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    RunCardComposeWidget(title = "Tables", modifier = modifier) {
        if (lastRoll != null) {
            Text(
                text = lastRoll,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyBlue,
            )
        }
        if (tables.isEmpty()) {
            Text(
                text = "Create weighted tables on the Tables screen.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tables.forEach { table ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = table.name,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { onInteraction(RunInteraction.TableRollSelected(table.tableId)) }
                        ) {
                            Text("Roll")
                        }
                    }
                }
            }
        }
    }
}
