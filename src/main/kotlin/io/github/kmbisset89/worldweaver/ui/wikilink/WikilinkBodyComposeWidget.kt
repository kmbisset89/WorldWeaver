package io.github.kmbisset89.worldweaver.ui.wikilink

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.WikilinkDisplaySpan
import io.github.kmbisset89.worldweaver.domain.WikilinkTarget
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun WikilinkBodyComposeWidget(
    spans: List<WikilinkDisplaySpan>,
    onTargetSelected: (WikilinkTarget) -> Unit,
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 14,
) {
    if (spans.isEmpty()) {
        return
    }
    val annotated = buildAnnotatedString {
        spans.forEachIndexed { index, span ->
            when {
                span.target != null -> {
                    pushStringAnnotation(TAG, index.toString())
                    withStyle(
                        SpanStyle(
                            color = NavyBlue,
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
                        append(span.text)
                    }
                    pop()
                }
                span.unresolved -> {
                    withStyle(SpanStyle(color = TextSecondary)) {
                        append(span.text)
                    }
                }
                else -> {
                    withStyle(SpanStyle(color = TextPrimary)) {
                        append(span.text)
                    }
                }
            }
        }
    }
    ClickableText(
        text = annotated,
        modifier = modifier,
        style = androidx.compose.ui.text.TextStyle(
            fontSize = fontSizeSp.sp,
            color = TextPrimary,
        ),
        onClick = { offset ->
            annotated.getStringAnnotations(TAG, offset, offset)
                .firstOrNull()
                ?.let { annotation ->
                    val index = annotation.item.toIntOrNull() ?: return@ClickableText
                    spans.getOrNull(index)?.target?.let(onTargetSelected)
                }
        },
    )
}

private const val TAG = "wikilink"
