package net.tactware.worldweaver.ui.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.tactware.worldweaver.domain.DieSides
import net.tactware.worldweaver.ui.theme.NavyBlue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
internal fun DieFaceComposeWidget(
    die: DieSides,
    colorStyle: DiceColorStyle,
    modifier: Modifier = Modifier,
    face: Int? = null,
    caption: String? = null,
    size: Dp = 56.dp,
    selected: Boolean = false,
    dimmed: Boolean = false,
    rollToken: Long = 0L,
    staggerIndex: Int = 0,
    animate: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val rotation = remember { Animatable(0f) }
    var displayedFace by remember { mutableStateOf(face) }

    LaunchedEffect(rollToken, face, animate) {
        if (!animate || rollToken == 0L) {
            displayedFace = face
            rotation.snapTo(0f)
            return@LaunchedEffect
        }
        delay(staggerIndex * STAGGER_MS)
        rotation.snapTo(0f)
        val tumble = launch {
            repeat(TUMBLE_FRAMES) {
                displayedFace = Random.nextInt(1, die.sides + 1)
                delay(FRAME_MS)
            }
            displayedFace = face
        }
        rotation.animateTo(
            targetValue = 720f,
            animationSpec = tween(durationMillis = TUMBLE_MS, easing = FastOutSlowInEasing),
        )
        tumble.join()
        rotation.snapTo(0f)
    }

    val clickable = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    val selectedBorder = if (selected) {
        Modifier.border(2.dp, NavyBlue, RoundedCornerShape(10.dp))
    } else {
        Modifier
    }
    val alpha = if (dimmed) 0.42f else 1f
    Box(
        modifier = modifier
            .size(size)
            .then(selectedBorder)
            .then(clickable)
            .padding(4.dp)
            .graphicsLayer {
                rotationZ = rotation.value
                rotationY = rotation.value * 0.25f
                val pulse = 1f + 0.06f * sin(rotation.value * PI.toFloat() / 180f)
                scaleX = pulse
                scaleY = pulse
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDieBody(die = die, body = colorStyle.body, pip = colorStyle.pip)
            if (shouldDrawPips(die, caption, displayedFace)) {
                drawPips(face = displayedFace ?: 1, color = colorStyle.pip)
            }
        }
        if (!shouldDrawPips(die, caption, displayedFace)) {
            val label = caption ?: displayedFace?.toString() ?: die.label
            Text(
                text = label,
                color = colorStyle.pip,
                fontWeight = FontWeight.Bold,
                fontSize = if (size >= 72.dp) 22.sp else 13.sp,
            )
        }
    }
}

private fun shouldDrawPips(
    die: DieSides,
    caption: String?,
    face: Int?,
): Boolean {
    return die == DieSides.D6 && caption == null && face != null && face in 1..6
}

private fun DrawScope.drawDieBody(
    die: DieSides,
    body: Color,
    pip: Color,
) {
    val outline = pip.copy(alpha = 0.55f)
    val stroke = Stroke(width = size.minDimension * 0.045f)
    when (die) {
        DieSides.D4 -> {
            val path = regularPolygon(sides = 3, rotationDegrees = -90f)
            drawPath(path, body, style = Fill)
            drawPath(path, outline, style = stroke)
        }
        DieSides.D6 -> {
            val inset = size.minDimension * 0.06f
            drawRoundRect(
                color = body,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(inset * 1.4f),
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(inset * 1.4f),
                style = stroke,
            )
        }
        DieSides.D8 -> {
            val path = regularPolygon(sides = 4, rotationDegrees = 0f)
            drawPath(path, body, style = Fill)
            drawPath(path, outline, style = stroke)
        }
        DieSides.D10 -> {
            val path = kitePath()
            drawPath(path, body, style = Fill)
            drawPath(path, outline, style = stroke)
        }
        DieSides.D12 -> {
            val path = regularPolygon(sides = 5, rotationDegrees = -90f)
            drawPath(path, body, style = Fill)
            drawPath(path, outline, style = stroke)
        }
        DieSides.D20 -> {
            val path = regularPolygon(sides = 6, rotationDegrees = -90f)
            drawPath(path, body, style = Fill)
            drawPath(path, outline, style = stroke)
        }
        DieSides.D100 -> {
            val inset = size.minDimension * 0.06f
            drawOval(
                color = body,
                topLeft = Offset(inset, inset * 1.15f),
                size = Size(size.width - inset * 2, size.height - inset * 2.3f),
            )
            drawOval(
                color = outline,
                topLeft = Offset(inset, inset * 1.15f),
                size = Size(size.width - inset * 2, size.height - inset * 2.3f),
                style = stroke,
            )
        }
    }
}

private fun DrawScope.regularPolygon(
    sides: Int,
    rotationDegrees: Float,
): Path {
    val radius = size.minDimension / 2f * 0.92f
    val center = Offset(size.width / 2f, size.height / 2f)
    val path = Path()
    repeat(sides) { index ->
        val angle = (rotationDegrees + index * 360f / sides) * PI.toFloat() / 180f
        val point = Offset(
            x = center.x + radius * cos(angle),
            y = center.y + radius * sin(angle),
        )
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
    path.close()
    return path
}

private fun DrawScope.kitePath(): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f * 0.94f
    return Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r * 0.72f, cy + r * 0.08f)
        lineTo(cx, cy + r)
        lineTo(cx - r * 0.72f, cy + r * 0.08f)
        close()
    }
}

private fun DrawScope.drawPips(
    face: Int,
    color: Color,
) {
    val radius = size.minDimension * 0.07f
    val positions = pipPositions(face)
    positions.forEach { (nx, ny) ->
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * nx, size.height * ny),
        )
    }
}

private fun pipPositions(face: Int): List<Pair<Float, Float>> {
    val left = 0.28f
    val mid = 0.5f
    val right = 0.72f
    val top = 0.28f
    val center = 0.5f
    val bottom = 0.72f
    return when (face) {
        1 -> listOf(mid to center)
        2 -> listOf(left to top, right to bottom)
        3 -> listOf(left to top, mid to center, right to bottom)
        4 -> listOf(left to top, right to top, left to bottom, right to bottom)
        5 -> listOf(left to top, right to top, mid to center, left to bottom, right to bottom)
        else -> listOf(
            left to top,
            left to center,
            left to bottom,
            right to top,
            right to center,
            right to bottom,
        )
    }
}

private const val TUMBLE_MS = 600
private const val TUMBLE_FRAMES = 10
private const val FRAME_MS = 45L
private const val STAGGER_MS = 45L
