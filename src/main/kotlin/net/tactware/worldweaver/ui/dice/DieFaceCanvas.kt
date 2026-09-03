package net.tactware.worldweaver.ui.dice

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import net.tactware.worldweaver.domain.DieSides
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val NaturalTwentyGold = Color(0xFFFFC53D)
internal val NaturalOneEmber = Color(0xFFE11D48)

internal fun DrawScope.drawPolishedDie(
    die: DieSides,
    body: Color,
    pip: Color,
    face: Int?,
    caption: String?,
    glow: Float,
    sparkle: Float,
    naturalTwenty: Boolean,
    naturalOne: Boolean,
    includeShadow: Boolean = true,
) {
    val highlight = mix(body, Color.White, 0.38f)
    val shade = mix(body, Color.Black, 0.32f)
    val deep = mix(body, Color.Black, 0.48f)
    val center = Offset(size.width / 2f, size.height / 2f)

    if (includeShadow) {
        drawDieContactShadow(airborne = 0f)
    }
    drawCritGlow(
        center = center,
        glow = glow,
        naturalTwenty = naturalTwenty,
        naturalOne = naturalOne,
    )
    drawDieBody(
        die = die,
        body = body,
        highlight = highlight,
        shade = shade,
        deep = deep,
        pip = pip,
    )
    if (shouldDrawPips(die, caption, face)) {
        drawPips(face = face ?: 1, color = pip)
    }
    drawCritSparkles(
        center = center,
        sparkle = sparkle,
        naturalTwenty = naturalTwenty,
    )
}

internal fun shouldDrawPips(
    die: DieSides,
    caption: String?,
    face: Int?,
): Boolean {
    return die == DieSides.D6 && caption == null && face != null && face in 1..6
}

internal fun DrawScope.drawDieContactShadow(airborne: Float) {
    val lift = airborne.coerceIn(0f, 1f)
    val shrink = 1f - lift * 0.38f
    val width = size.width * 0.64f * shrink
    val height = size.height * 0.14f * shrink
    drawOval(
        color = Color.Black.copy(alpha = 0.22f * (1f - lift * 0.6f)),
        topLeft = Offset((size.width - width) / 2f, size.height * 0.80f + size.height * 0.05f * lift),
        size = Size(width, height),
    )
}

private fun DrawScope.drawCritGlow(
    center: Offset,
    glow: Float,
    naturalTwenty: Boolean,
    naturalOne: Boolean,
) {
    if (glow <= 0.01f) {
        return
    }
    val color = when {
        naturalTwenty -> NaturalTwentyGold
        naturalOne -> NaturalOneEmber
        else -> return
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.42f * glow),
                color.copy(alpha = 0.12f * glow),
                Color.Transparent,
            ),
            center = center,
            radius = size.minDimension * 0.58f,
        ),
        radius = size.minDimension * (0.46f + 0.08f * glow),
        center = center,
    )
}

private fun DrawScope.drawCritSparkles(
    center: Offset,
    sparkle: Float,
    naturalTwenty: Boolean,
) {
    if (!naturalTwenty || sparkle <= 0.02f) {
        return
    }
    val count = 10
    repeat(count) { index ->
        val angle = (index * 36f + sparkle * 48f) * PI.toFloat() / 180f
        val orbit = size.minDimension * (0.40f + 0.07f * sin(sparkle * PI.toFloat() + index))
        val point = Offset(
            x = center.x + cos(angle) * orbit,
            y = center.y + sin(angle) * orbit,
        )
        val sparkSize = size.minDimension * 0.034f * (0.45f + sparkle)
        drawPath(
            path = diamondPath(point, sparkSize),
            color = NaturalTwentyGold.copy(alpha = 0.35f + 0.65f * sparkle),
        )
    }
}

private fun DrawScope.drawDieBody(
    die: DieSides,
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
) {
    val outline = Stroke(
        width = size.minDimension * 0.038f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    when (die) {
        DieSides.D4 -> drawD4(body, highlight, shade, deep, pip, outline)
        DieSides.D6 -> drawD6(body, highlight, shade, deep, pip, outline)
        DieSides.D8 -> drawD8(body, highlight, shade, deep, pip, outline)
        DieSides.D10 -> drawD10(body, highlight, shade, deep, pip, outline)
        DieSides.D12 -> drawD12(body, highlight, shade, deep, pip, outline)
        DieSides.D20 -> drawD20(body, highlight, shade, deep, pip, outline)
        DieSides.D100 -> drawD100(body, highlight, shade, deep, pip, outline)
    }
}

private fun DrawScope.drawD4(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val outer = regularPolygon(sides = 3, rotationDegrees = -90f, radiusScale = 0.92f)
    val centroid = Offset(size.width / 2f, size.height * 0.56f)
    val vertices = polygonPoints(sides = 3, rotationDegrees = -90f, radiusScale = 0.92f)
    drawPath(outer, bodyBrush(highlight, body, shade), style = Fill)
    drawPath(triangle(vertices[0], vertices[1], centroid), highlight.copy(alpha = 0.28f))
    drawPath(triangle(vertices[1], vertices[2], centroid), shade.copy(alpha = 0.28f))
    drawPath(triangle(vertices[2], vertices[0], centroid), deep.copy(alpha = 0.16f))
    drawPath(outer, pip.copy(alpha = 0.55f), style = outline)
    drawSpecular(Offset(size.width * 0.42f, size.height * 0.34f), 0.10f)
}

private fun DrawScope.drawD6(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val inset = size.minDimension * 0.07f
    val topLeft = Offset(inset, inset)
    val rectSize = Size(size.width - inset * 2, size.height - inset * 2)
    val radius = CornerRadius(inset * 1.55f)
    drawRoundRect(
        brush = bodyBrush(highlight, body, shade),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = radius,
    )
    drawRoundRect(
        color = highlight.copy(alpha = 0.22f),
        topLeft = Offset(inset + inset * 0.35f, inset + inset * 0.35f),
        size = Size(rectSize.width * 0.72f, rectSize.height * 0.22f),
        cornerRadius = CornerRadius(inset),
    )
    drawRoundRect(
        color = deep.copy(alpha = 0.20f),
        topLeft = Offset(inset + rectSize.width * 0.12f, inset + rectSize.height * 0.72f),
        size = Size(rectSize.width * 0.78f, rectSize.height * 0.20f),
        cornerRadius = CornerRadius(inset),
    )
    val plateInset = inset * 1.55f
    drawRoundRect(
        color = mix(body, Color.Black, 0.08f).copy(alpha = 0.18f),
        topLeft = Offset(plateInset, plateInset),
        size = Size(size.width - plateInset * 2, size.height - plateInset * 2),
        cornerRadius = CornerRadius(inset * 1.1f),
    )
    drawRoundRect(
        color = pip.copy(alpha = 0.55f),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = radius,
        style = outline,
    )
    drawSpecular(Offset(size.width * 0.30f, size.height * 0.28f), 0.12f)
}

private fun DrawScope.drawD8(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val diamond = regularPolygon(sides = 4, rotationDegrees = 0f, radiusScale = 0.92f)
    val points = polygonPoints(sides = 4, rotationDegrees = 0f, radiusScale = 0.92f)
    val center = Offset(size.width / 2f, size.height / 2f)
    drawPath(diamond, bodyBrush(highlight, body, shade), style = Fill)
    drawPath(triangle(points[0], points[1], center), highlight.copy(alpha = 0.26f))
    drawPath(triangle(points[1], points[2], center), shade.copy(alpha = 0.22f))
    drawPath(triangle(points[2], points[3], center), deep.copy(alpha = 0.20f))
    drawPath(triangle(points[3], points[0], center), highlight.copy(alpha = 0.10f))
    drawPath(diamond, pip.copy(alpha = 0.55f), style = outline)
    drawLine(
        color = pip.copy(alpha = 0.22f),
        start = points[0],
        end = points[2],
        strokeWidth = size.minDimension * 0.018f,
    )
    drawSpecular(Offset(size.width * 0.40f, size.height * 0.30f), 0.09f)
}

private fun DrawScope.drawD10(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val kite = kitePath()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f * 0.94f
    val top = Offset(cx, cy - r)
    val right = Offset(cx + r * 0.72f, cy + r * 0.08f)
    val bottom = Offset(cx, cy + r)
    val left = Offset(cx - r * 0.72f, cy + r * 0.08f)
    val waist = Offset(cx, cy + r * 0.04f)
    drawPath(kite, bodyBrush(highlight, body, shade), style = Fill)
    drawPath(triangle(top, right, waist), highlight.copy(alpha = 0.24f))
    drawPath(triangle(top, left, waist), shade.copy(alpha = 0.12f))
    drawPath(triangle(bottom, right, waist), shade.copy(alpha = 0.28f))
    drawPath(triangle(bottom, left, waist), deep.copy(alpha = 0.22f))
    drawPath(kite, pip.copy(alpha = 0.55f), style = outline)
    drawLine(pip.copy(alpha = 0.20f), top, bottom, strokeWidth = size.minDimension * 0.016f)
    drawSpecular(Offset(size.width * 0.42f, size.height * 0.28f), 0.08f)
}

private fun DrawScope.drawD12(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val outer = regularPolygon(sides = 5, rotationDegrees = -90f, radiusScale = 0.92f)
    val inner = regularPolygon(sides = 5, rotationDegrees = -54f, radiusScale = 0.46f)
    val outerPoints = polygonPoints(sides = 5, rotationDegrees = -90f, radiusScale = 0.92f)
    val innerPoints = polygonPoints(sides = 5, rotationDegrees = -54f, radiusScale = 0.46f)
    drawPath(outer, bodyBrush(highlight, body, shade), style = Fill)
    outerPoints.forEachIndexed { index, point ->
        val next = outerPoints[(index + 1) % outerPoints.size]
        val innerPoint = innerPoints[index]
        val facet = when (index % 3) {
            0 -> highlight.copy(alpha = 0.20f)
            1 -> shade.copy(alpha = 0.20f)
            else -> deep.copy(alpha = 0.16f)
        }
        drawPath(triangle(point, next, innerPoint), facet)
    }
    drawPath(inner, mix(body, Color.Black, 0.10f).copy(alpha = 0.22f))
    drawPath(outer, pip.copy(alpha = 0.55f), style = outline)
    drawPath(inner, pip.copy(alpha = 0.22f), style = Stroke(width = size.minDimension * 0.018f))
    drawSpecular(Offset(size.width * 0.38f, size.height * 0.30f), 0.09f)
}

private fun DrawScope.drawD20(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val outer = regularPolygon(sides = 6, rotationDegrees = -90f, radiusScale = 0.92f)
    val mid = regularPolygon(sides = 6, rotationDegrees = -60f, radiusScale = 0.58f)
    val plate = regularPolygon(sides = 6, rotationDegrees = -90f, radiusScale = 0.34f)
    val outerPoints = polygonPoints(sides = 6, rotationDegrees = -90f, radiusScale = 0.92f)
    val midPoints = polygonPoints(sides = 6, rotationDegrees = -60f, radiusScale = 0.58f)
    drawPath(outer, bodyBrush(highlight, body, shade), style = Fill)
    outerPoints.forEachIndexed { index, point ->
        val next = outerPoints[(index + 1) % outerPoints.size]
        val midPoint = midPoints[index]
        val facet = when (index % 3) {
            0 -> highlight.copy(alpha = 0.26f)
            1 -> shade.copy(alpha = 0.16f)
            else -> deep.copy(alpha = 0.18f)
        }
        drawPath(triangle(point, next, midPoint), facet)
    }
    drawPath(mid, mix(body, Color.White, 0.06f).copy(alpha = 0.16f))
    drawPath(plate, mix(body, Color.Black, 0.12f).copy(alpha = 0.20f))
    drawPath(outer, pip.copy(alpha = 0.55f), style = outline)
    drawPath(mid, pip.copy(alpha = 0.18f), style = Stroke(width = size.minDimension * 0.016f))
    drawSpecular(Offset(size.width * 0.36f, size.height * 0.28f), 0.10f)
}

private fun DrawScope.drawD100(
    body: Color,
    highlight: Color,
    shade: Color,
    deep: Color,
    pip: Color,
    outline: Stroke,
) {
    val inset = size.minDimension * 0.06f
    val topLeft = Offset(inset, inset * 1.15f)
    val ovalSize = Size(size.width - inset * 2, size.height - inset * 2.3f)
    drawOval(
        brush = bodyBrush(highlight, body, shade),
        topLeft = topLeft,
        size = ovalSize,
    )
    drawOval(
        color = deep.copy(alpha = 0.18f),
        topLeft = Offset(inset + ovalSize.width * 0.12f, inset * 1.15f + ovalSize.height * 0.58f),
        size = Size(ovalSize.width * 0.76f, ovalSize.height * 0.32f),
    )
    drawOval(
        color = highlight.copy(alpha = 0.22f),
        topLeft = Offset(inset + ovalSize.width * 0.16f, inset * 1.15f + ovalSize.height * 0.12f),
        size = Size(ovalSize.width * 0.52f, ovalSize.height * 0.22f),
    )
    val bandTop = inset * 1.15f + ovalSize.height * 0.42f
    drawRect(
        color = pip.copy(alpha = 0.16f),
        topLeft = Offset(inset, bandTop),
        size = Size(ovalSize.width, ovalSize.height * 0.16f),
    )
    drawOval(
        color = pip.copy(alpha = 0.55f),
        topLeft = topLeft,
        size = ovalSize,
        style = outline,
    )
    drawSpecular(Offset(size.width * 0.34f, size.height * 0.32f), 0.11f)
}

private fun DrawScope.drawPips(
    face: Int,
    color: Color,
) {
    val radius = size.minDimension * 0.068f
    val crater = mix(color, Color.Black, 0.35f)
    val shine = mix(color, Color.White, 0.45f)
    pipPositions(face).forEach { (nx, ny) ->
        val center = Offset(size.width * nx, size.height * ny)
        drawCircle(
            color = crater.copy(alpha = 0.55f),
            radius = radius * 1.08f,
            center = center + Offset(radius * 0.12f, radius * 0.16f),
        )
        drawCircle(color = color, radius = radius, center = center)
        drawCircle(
            color = shine.copy(alpha = 0.55f),
            radius = radius * 0.32f,
            center = center + Offset(-radius * 0.28f, -radius * 0.28f),
        )
    }
}

private fun DrawScope.drawSpecular(center: Offset, radiusScale: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.42f), Color.Transparent),
            center = center,
            radius = size.minDimension * radiusScale,
        ),
        radius = size.minDimension * radiusScale,
        center = center,
    )
}

private fun DrawScope.bodyBrush(
    highlight: Color,
    body: Color,
    shade: Color,
): Brush {
    return Brush.linearGradient(
        colors = listOf(highlight, body, shade),
        start = Offset(size.width * 0.18f, size.height * 0.12f),
        end = Offset(size.width * 0.88f, size.height * 0.92f),
    )
}

private fun DrawScope.regularPolygon(
    sides: Int,
    rotationDegrees: Float,
    radiusScale: Float,
): Path {
    return pathFromPoints(polygonPoints(sides, rotationDegrees, radiusScale))
}

private fun DrawScope.polygonPoints(
    sides: Int,
    rotationDegrees: Float,
    radiusScale: Float,
): List<Offset> {
    val radius = size.minDimension / 2f * radiusScale
    val center = Offset(size.width / 2f, size.height / 2f)
    return List(sides) { index ->
        val angle = (rotationDegrees + index * 360f / sides) * PI.toFloat() / 180f
        Offset(
            x = center.x + radius * cos(angle),
            y = center.y + radius * sin(angle),
        )
    }
}

private fun DrawScope.kitePath(): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f * 0.94f
    return pathFromPoints(
        listOf(
            Offset(cx, cy - r),
            Offset(cx + r * 0.72f, cy + r * 0.08f),
            Offset(cx, cy + r),
            Offset(cx - r * 0.72f, cy + r * 0.08f),
        ),
    )
}

private fun diamondPath(center: Offset, radius: Float): Path {
    return pathFromPoints(
        listOf(
            Offset(center.x, center.y - radius),
            Offset(center.x + radius * 0.62f, center.y),
            Offset(center.x, center.y + radius),
            Offset(center.x - radius * 0.62f, center.y),
        ),
    )
}

private fun triangle(a: Offset, b: Offset, c: Offset): Path {
    return pathFromPoints(listOf(a, b, c))
}

private fun pathFromPoints(points: List<Offset>): Path {
    val path = Path()
    points.forEachIndexed { index, point ->
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
    path.close()
    return path
}

private fun pipPositions(face: Int): List<Pair<Float, Float>> {
    val left = 0.30f
    val mid = 0.5f
    val right = 0.70f
    val top = 0.30f
    val center = 0.5f
    val bottom = 0.70f
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

private fun mix(from: Color, to: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * t,
        green = from.green + (to.green - from.green) * t,
        blue = from.blue + (to.blue - from.blue) * t,
        alpha = from.alpha + (to.alpha - from.alpha) * t,
    )
}
