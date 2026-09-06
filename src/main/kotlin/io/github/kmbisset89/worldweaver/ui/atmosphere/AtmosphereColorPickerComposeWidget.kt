package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.domain.GoveeColorHexParser
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

/**
 * HSV square and hue bar that emit `#RRGGBB` as the pointer moves.
 */
@Composable
internal fun AtmosphereColorPickerComposeWidget(
    colorHex: String,
    onColorHexChanged: (String) -> Unit,
) {
    var hsv by remember {
        mutableStateOf(hexToHsv(colorHex) ?: DEFAULT_HSV)
    }
    LaunchedEffect(colorHex) {
        hexToHsv(colorHex)?.let { hsv = it }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SaturationValueSquare(
            hue = hsv.hue,
            saturation = hsv.saturation,
            value = hsv.value,
            onSaturationValueChanged = { saturation, value ->
                onColorHexChanged(hsvToHex(hsv.hue, saturation, value))
            },
        )
        HueBar(
            hue = hsv.hue,
            saturation = hsv.saturation,
            value = hsv.value,
            onHueChanged = { hue ->
                onColorHexChanged(hsvToHex(hue, hsv.saturation, hsv.value))
            },
        )
    }
}

@Composable
private fun SaturationValueSquare(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChanged: (Float, Float) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(SV_SIZE)
            .clip(shape)
            .border(1.dp, TextSecondary.copy(alpha = 0.35f), shape)
            .semantics { contentDescription = "Saturation and brightness" }
            .pointerInput(hue) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun emit(position: Offset) {
                        val nextSaturation = (position.x / size.width).coerceIn(0f, 1f)
                        val nextValue = 1f - (position.y / size.height).coerceIn(0f, 1f)
                        onSaturationValueChanged(nextSaturation, nextValue)
                    }
                    emit(down.position)
                    drag(down.id) { change ->
                        change.consume()
                        emit(change.position)
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val hueColor = Color.hsv(wrapHue(hue), 1f, 1f)
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, hueColor)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val thumb = Offset(
                x = saturation.coerceIn(0f, 1f) * size.width,
                y = (1f - value.coerceIn(0f, 1f)) * size.height,
            )
            drawCircle(
                color = Color.Black,
                radius = 8.dp.toPx(),
                center = thumb,
                style = Stroke(width = 3.dp.toPx()),
            )
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = thumb,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

@Composable
private fun HueBar(
    hue: Float,
    saturation: Float,
    value: Float,
    onHueChanged: (Float) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(width = HUE_BAR_WIDTH, height = SV_SIZE)
            .clip(shape)
            .border(1.dp, TextSecondary.copy(alpha = 0.35f), shape)
            .semantics { contentDescription = "Hue" }
            .pointerInput(saturation, value) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun emit(position: Offset) {
                        val nextHue = (position.y / size.height).coerceIn(0f, 1f) * 360f
                        onHueChanged(wrapHue(nextHue))
                    }
                    emit(down.position)
                    drag(down.id) { change ->
                        change.consume()
                        emit(change.position)
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = Brush.verticalGradient(HUE_SPECTRUM))
            val thumbY = (wrapHue(hue) / 360f) * size.height
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, thumbY - 3.dp.toPx()),
                size = Size(size.width, 6.dp.toPx()),
                style = Stroke(width = 3.dp.toPx()),
            )
            drawRect(
                color = Color.White,
                topLeft = Offset(1.dp.toPx(), thumbY - 2.dp.toPx()),
                size = Size(size.width - 2.dp.toPx(), 4.dp.toPx()),
            )
        }
    }
}

private data class Hsv(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

private fun hexToHsv(colorHex: String): Hsv? {
    val rgb = HEX_PARSER.parse(colorHex) ?: return null
    return rgbToHsv(rgb.red, rgb.green, rgb.blue)
}

private fun rgbToHsv(red: Int, green: Int, blue: Int): Hsv {
    val r = red / 255f
    val g = green / 255f
    val b = blue / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val hue = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    val wrapped = if (hue < 0f) hue + 360f else hue
    val saturation = if (max == 0f) 0f else delta / max
    return Hsv(hue = wrapped, saturation = saturation, value = max)
}

private fun hsvToHex(hue: Float, saturation: Float, value: Float): String {
    val color = Color.hsv(
        hue = wrapHue(hue),
        saturation = saturation.coerceIn(0f, 1f),
        value = value.coerceIn(0f, 1f),
    )
    val argb = color.toArgb()
    val red = (argb shr 16) and 0xFF
    val green = (argb shr 8) and 0xFF
    val blue = argb and 0xFF
    return "#%02X%02X%02X".format(red, green, blue)
}

private fun wrapHue(hue: Float): Float {
    val wrapped = hue.mod(360f)
    return if (wrapped < 0f) wrapped + 360f else wrapped
}

private val HEX_PARSER = GoveeColorHexParser()
private val DEFAULT_HSV = Hsv(hue = 28f, saturation = 0.61f, value = 0.89f)
private val SV_SIZE = 168.dp
private val HUE_BAR_WIDTH = 22.dp
private val HUE_SPECTRUM = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
    Color.Red,
)
