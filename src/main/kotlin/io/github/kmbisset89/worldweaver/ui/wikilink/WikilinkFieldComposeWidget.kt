package io.github.kmbisset89.worldweaver.ui.wikilink

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.WikilinkTarget
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun WikilinkFieldComposeWidget(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<WikilinkTarget>,
    onSuggestionSelected: (WikilinkTarget) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 3,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            supportingText = supportingText,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth(),
        )
        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(
                        text = "Link to",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    suggestions.forEach { target ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(target) }
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                text = target.title,
                                fontSize = 13.sp,
                                color = NavyBlue,
                            )
                            Text(
                                text = target.kind.searchKind.displayName,
                                fontSize = 11.sp,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
