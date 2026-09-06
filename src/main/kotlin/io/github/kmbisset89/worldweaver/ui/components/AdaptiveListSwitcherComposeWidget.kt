package io.github.kmbisset89.worldweaver.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun AdaptiveListSwitcherComposeWidget(
    selectedName: String,
    listLabel: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = "Show $listLabel, $selectedName"
        },
    ) {
        Icon(Icons.Default.Menu, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(
            text = selectedName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp),
        )
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
    }
}
