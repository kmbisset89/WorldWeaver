package io.github.kmbisset89.worldweaver.domain

import kotlin.random.Random

internal class DiceRoller(
    private val nextFace: (sides: Int) -> Int = { sides -> Random.nextInt(1, sides + 1) },
) {
    fun roll(request: DiceRollRequest): DiceRollResult {
        val sides = request.sides.coerceAtLeast(2)
        val count = request.count.coerceAtLeast(1)
        val mode = resolvedMode(sides = sides, count = count, mode = request.mode)
        val faces = when (mode) {
            RollMode.Normal -> List(count) { nextFace(sides).coerceIn(1, sides) }
            RollMode.Advantage, RollMode.Disadvantage -> listOf(
                nextFace(sides).coerceIn(1, sides),
                nextFace(sides).coerceIn(1, sides),
            )
        }
        val keptFaces = when (mode) {
            RollMode.Normal -> faces
            RollMode.Advantage -> listOf(faces.max())
            RollMode.Disadvantage -> listOf(faces.min())
        }
        return DiceRollResult(
            sides = sides,
            count = count,
            modifier = request.modifier,
            mode = mode,
            faces = faces,
            keptFaces = keptFaces,
            total = keptFaces.sum() + request.modifier,
            source = DiceRollSource.Automated,
        )
    }

    fun record(request: DiceRollRequest, faces: List<Int>): DiceRollResult? {
        val sides = request.sides.coerceAtLeast(2)
        val count = request.count.coerceAtLeast(1)
        val mode = resolvedMode(sides = sides, count = count, mode = request.mode)
        val expectedCount = when (mode) {
            RollMode.Normal -> count
            RollMode.Advantage, RollMode.Disadvantage -> 2
        }
        if (faces.size != expectedCount) {
            return null
        }
        if (faces.any { face -> face !in 1..sides }) {
            return null
        }
        val keptFaces = when (mode) {
            RollMode.Normal -> faces
            RollMode.Advantage -> listOf(faces.max())
            RollMode.Disadvantage -> listOf(faces.min())
        }
        return DiceRollResult(
            sides = sides,
            count = count,
            modifier = request.modifier,
            mode = mode,
            faces = faces,
            keptFaces = keptFaces,
            total = keptFaces.sum() + request.modifier,
            source = DiceRollSource.Manual,
        )
    }

    private fun resolvedMode(
        sides: Int,
        count: Int,
        mode: RollMode,
    ): RollMode {
        val advantageAllowed = sides == DieSides.D20.sides && count == 1
        return if (advantageAllowed) mode else RollMode.Normal
    }
}
