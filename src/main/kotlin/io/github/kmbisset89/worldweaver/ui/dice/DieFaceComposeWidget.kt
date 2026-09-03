package io.github.kmbisset89.worldweaver.ui.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import kotlin.math.abs
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
    val spinZ = remember { Animatable(0f) }
    val spinY = remember { Animatable(0f) }
    val spinX = remember { Animatable(0f) }
    val lift = remember { Animatable(0f) }
    val stretchX = remember { Animatable(1f) }
    val stretchY = remember { Animatable(1f) }
    val glow = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val sparkle = remember { Animatable(0f) }
    val faceAlpha = remember { Animatable(1f) }
    var displayedFace by remember { mutableStateOf(face) }

    LaunchedEffect(rollToken, face, animate, die) {
        val naturalTwenty = isNaturalTwentyFace(die, face)
        val naturalOne = isNaturalOneFace(die, face)
        if (!animate || rollToken == 0L) {
            displayedFace = face
            restPose(
                spinZ = spinZ,
                spinY = spinY,
                spinX = spinX,
                lift = lift,
                stretchX = stretchX,
                stretchY = stretchY,
                shake = shake,
                faceAlpha = faceAlpha,
                glow = glow,
                sparkle = sparkle,
                naturalTwenty = naturalTwenty,
                naturalOne = naturalOne,
            )
            return@LaunchedEffect
        }
        delay(staggerIndex * STAGGER_MS)
        playRoll(
            die = die,
            finalFace = face,
            staggerIndex = staggerIndex,
            naturalTwenty = naturalTwenty,
            naturalOne = naturalOne,
            displayedFace = { displayedFace = it },
            spinZ = spinZ,
            spinY = spinY,
            spinX = spinX,
            lift = lift,
            stretchX = stretchX,
            stretchY = stretchY,
            glow = glow,
            shake = shake,
            sparkle = sparkle,
            faceAlpha = faceAlpha,
        )
    }

    val clickable = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    val selectedBorder = if (selected) {
        Modifier
            .background(NavyBlue.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
            .border(2.dp, NavyBlue, RoundedCornerShape(10.dp))
    } else {
        Modifier
    }
    val alpha = if (dimmed) 0.42f else 1f
    val naturalTwenty = isNaturalTwentyFace(die, displayedFace)
    val naturalOne = isNaturalOneFace(die, displayedFace)
    val airborne = (abs(lift.value) / HOP_CEILING).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .size(size)
            .then(selectedBorder)
            .then(clickable)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDieContactShadow(airborne = airborne)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = spinZ.value
                    rotationY = spinY.value
                    rotationX = spinX.value
                    translationY = lift.value
                    translationX = shake.value
                    scaleX = stretchX.value
                    scaleY = stretchY.value
                    cameraDistance = 14f * density
                    this.alpha = alpha
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPolishedDie(
                    die = die,
                    body = colorStyle.body,
                    pip = colorStyle.pip,
                    face = displayedFace,
                    caption = caption,
                    glow = glow.value,
                    sparkle = sparkle.value,
                    naturalTwenty = naturalTwenty,
                    naturalOne = naturalOne,
                    includeShadow = false,
                )
            }
            if (!shouldDrawPips(die, caption, displayedFace)) {
                val label = caption ?: displayedFace?.toString() ?: die.label
                val numberColor = when {
                    naturalTwenty -> NaturalTwentyGold
                    naturalOne -> NaturalOneEmber
                    else -> colorStyle.pip
                }
                Text(
                    text = label,
                    color = numberColor.copy(alpha = numberColor.alpha * faceAlpha.value),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (size >= 72.dp) 22.sp else 13.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = colorStyle.body.copy(alpha = 0.55f * faceAlpha.value),
                            offset = Offset(1f, 1.5f),
                            blurRadius = 3f,
                        ),
                    ),
                )
            }
        }
    }
}

private suspend fun restPose(
    spinZ: Animatable<Float, AnimationVector1D>,
    spinY: Animatable<Float, AnimationVector1D>,
    spinX: Animatable<Float, AnimationVector1D>,
    lift: Animatable<Float, AnimationVector1D>,
    stretchX: Animatable<Float, AnimationVector1D>,
    stretchY: Animatable<Float, AnimationVector1D>,
    shake: Animatable<Float, AnimationVector1D>,
    faceAlpha: Animatable<Float, AnimationVector1D>,
    glow: Animatable<Float, AnimationVector1D>,
    sparkle: Animatable<Float, AnimationVector1D>,
    naturalTwenty: Boolean,
    naturalOne: Boolean,
) {
    spinZ.snapTo(0f)
    spinY.snapTo(0f)
    spinX.snapTo(0f)
    lift.snapTo(0f)
    stretchX.snapTo(1f)
    stretchY.snapTo(1f)
    shake.snapTo(0f)
    faceAlpha.snapTo(1f)
    glow.snapTo(
        when {
            naturalTwenty -> 0.45f
            naturalOne -> 0.28f
            else -> 0f
        },
    )
    sparkle.snapTo(if (naturalTwenty) 0.28f else 0f)
}

private suspend fun playRoll(
    die: DieSides,
    finalFace: Int?,
    staggerIndex: Int,
    naturalTwenty: Boolean,
    naturalOne: Boolean,
    displayedFace: (Int?) -> Unit,
    spinZ: Animatable<Float, AnimationVector1D>,
    spinY: Animatable<Float, AnimationVector1D>,
    spinX: Animatable<Float, AnimationVector1D>,
    lift: Animatable<Float, AnimationVector1D>,
    stretchX: Animatable<Float, AnimationVector1D>,
    stretchY: Animatable<Float, AnimationVector1D>,
    glow: Animatable<Float, AnimationVector1D>,
    shake: Animatable<Float, AnimationVector1D>,
    sparkle: Animatable<Float, AnimationVector1D>,
    faceAlpha: Animatable<Float, AnimationVector1D>,
) {
    val direction = if (staggerIndex % 2 == 0) 1f else -1f
    val hop = -(28f + (staggerIndex % 3) * 6f)
    val spinTurns = if (staggerIndex % 2 == 0) 360f else 720f
    restPose(
        spinZ = spinZ,
        spinY = spinY,
        spinX = spinX,
        lift = lift,
        stretchX = stretchX,
        stretchY = stretchY,
        shake = shake,
        faceAlpha = faceAlpha,
        glow = glow,
        sparkle = sparkle,
        naturalTwenty = false,
        naturalOne = false,
    )
    coroutineScope {
        launch { faceAlpha.animateTo(0.22f, tween(durationMillis = 80)) }
        launch { stretchX.animateTo(1.10f, tween(durationMillis = 90)) }
        launch { stretchY.animateTo(0.86f, tween(durationMillis = 90)) }
        spinZ.animateTo(-18f * direction, tween(durationMillis = 90))
    }
    coroutineScope {
        launch {
            lift.animateTo(
                targetValue = hop,
                animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
            )
        }
        launch { stretchX.animateTo(0.90f, tween(durationMillis = 140)) }
        launch { stretchY.animateTo(1.14f, tween(durationMillis = 140)) }
        launch {
            spinZ.animateTo(
                targetValue = spinTurns * direction,
                animationSpec = tween(durationMillis = TUMBLE_MS, easing = FastOutSlowInEasing),
            )
        }
        launch {
            spinY.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = TUMBLE_MS
                    0f at 0
                    (150f * direction) at (TUMBLE_MS * 0.4).toInt()
                    0f at TUMBLE_MS using LinearOutSlowInEasing
                },
            )
        }
        launch {
            spinX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = TUMBLE_MS
                    0f at 0
                    (-100f * direction) at (TUMBLE_MS * 0.45).toInt()
                    0f at TUMBLE_MS using LinearOutSlowInEasing
                },
            )
        }
        launch {
            tumbleFaces(
                die = die,
                finalFace = finalFace,
                onFace = displayedFace,
            )
        }
        delay(AIR_MS)
        lift.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = 0.52f,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }
    coroutineScope {
        launch { faceAlpha.animateTo(1f, tween(durationMillis = 140)) }
        launch {
            spinY.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 340f),
            )
        }
        launch {
            spinX.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 340f),
            )
        }
        launch { stretchX.animateTo(1.16f, tween(durationMillis = 70)) }
        launch { stretchY.animateTo(0.80f, tween(durationMillis = 70)) }
    }
    coroutineScope {
        launch {
            stretchX.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        launch {
            stretchY.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }
    if (naturalTwenty) {
        playNaturalTwenty(
            direction = direction,
            spinZ = spinZ,
            lift = lift,
            stretchX = stretchX,
            stretchY = stretchY,
            glow = glow,
            sparkle = sparkle,
        )
    } else if (naturalOne) {
        playNaturalOne(
            stretchX = stretchX,
            stretchY = stretchY,
            shake = shake,
            glow = glow,
        )
    }
}

private suspend fun tumbleFaces(
    die: DieSides,
    finalFace: Int?,
    onFace: (Int?) -> Unit,
) {
    FACE_DELAYS.forEachIndexed { index, delayMs ->
        val nextFace = when {
            finalFace == null -> Random.nextInt(1, die.sides + 1)
            index >= FACE_DELAYS.lastIndex -> finalFace
            index == FACE_DELAYS.lastIndex - 1 -> nearFace(die, finalFace)
            else -> Random.nextInt(1, die.sides + 1)
        }
        onFace(nextFace)
        delay(delayMs)
    }
    onFace(finalFace)
}

private fun nearFace(die: DieSides, finalFace: Int): Int {
    val offset = if (finalFace % 2 == 0) 1 else -1
    val neighbor = finalFace + offset
    return when {
        neighbor < 1 -> die.sides
        neighbor > die.sides -> 1
        else -> neighbor
    }
}

private suspend fun playNaturalTwenty(
    direction: Float,
    spinZ: Animatable<Float, AnimationVector1D>,
    lift: Animatable<Float, AnimationVector1D>,
    stretchX: Animatable<Float, AnimationVector1D>,
    stretchY: Animatable<Float, AnimationVector1D>,
    glow: Animatable<Float, AnimationVector1D>,
    sparkle: Animatable<Float, AnimationVector1D>,
) {
    coroutineScope {
        launch {
            glow.animateTo(1f, tween(durationMillis = 160))
            glow.animateTo(0.55f, tween(durationMillis = 640))
        }
        launch {
            sparkle.animateTo(1f, tween(durationMillis = 280))
            sparkle.animateTo(0.40f, tween(durationMillis = 720))
        }
        launch {
            lift.animateTo(
                targetValue = -16f,
                animationSpec = tween(durationMillis = 130, easing = FastOutLinearInEasing),
            )
            lift.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.48f,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        launch {
            spinZ.animateTo(
                targetValue = spinZ.value + 360f * direction,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            )
        }
        launch { stretchX.animateTo(0.92f, tween(durationMillis = 120)) }
        launch { stretchY.animateTo(1.10f, tween(durationMillis = 120)) }
    }
    coroutineScope {
        launch {
            stretchX.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
            )
        }
        launch {
            stretchY.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
            )
        }
    }
}

private suspend fun playNaturalOne(
    stretchX: Animatable<Float, AnimationVector1D>,
    stretchY: Animatable<Float, AnimationVector1D>,
    shake: Animatable<Float, AnimationVector1D>,
    glow: Animatable<Float, AnimationVector1D>,
) {
    coroutineScope {
        launch {
            glow.animateTo(1f, tween(durationMillis = 110))
            glow.animateTo(0.32f, tween(durationMillis = 480))
        }
        launch { stretchX.animateTo(1.12f, tween(durationMillis = 80)) }
        launch { stretchY.animateTo(0.84f, tween(durationMillis = 80)) }
        SHAKE_OFFSETS.forEach { offset ->
            shake.animateTo(offset, tween(durationMillis = 38))
        }
        shake.snapTo(0f)
    }
    coroutineScope {
        launch {
            stretchX.animateTo(
                targetValue = 0.97f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
            )
        }
        launch {
            stretchY.animateTo(
                targetValue = 0.97f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
            )
        }
    }
}

private val FACE_DELAYS = longArrayOf(22, 24, 26, 30, 36, 44, 56, 72, 92, 120, 150)
private val SHAKE_OFFSETS = listOf(8f, -8f, 6f, -5f, 3f, 0f)

private const val TUMBLE_MS = 640
private const val AIR_MS = 220L
private const val STAGGER_MS = 70L
private const val HOP_CEILING = 40f
