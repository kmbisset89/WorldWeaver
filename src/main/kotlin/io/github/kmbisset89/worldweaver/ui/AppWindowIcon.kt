package io.github.kmbisset89.worldweaver.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import io.github.kmbisset89.worldweaver.generated.resources.Res
import io.github.kmbisset89.worldweaver.generated.resources.icon
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun appWindowIcon(): Painter = painterResource(Res.drawable.icon)
