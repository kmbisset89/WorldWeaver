package io.github.kmbisset89.worldweaver.domain

import java.util.Base64

internal class UniversalVttDocumentFactory {
    fun create(battleMap: BattleMap, originalPng: ByteArray): UniversalVttDocument {
        val columns = battleMap.columns.coerceAtLeast(1)
        val rows = battleMap.rows.coerceAtLeast(1)
        val pixelsPerGrid = (battleMap.originalWidth / columns).coerceAtLeast(1)
        return UniversalVttDocument(
            format = FORMAT_VERSION,
            resolution = UniversalVttDocument.Resolution(
                mapOrigin = UniversalVttDocument.Point(x = 0.0, y = 0.0),
                mapSize = UniversalVttDocument.Point(
                    x = columns.toDouble(),
                    y = rows.toDouble(),
                ),
                pixelsPerGrid = pixelsPerGrid,
            ),
            lineOfSight = emptyList(),
            objectsLineOfSight = emptyList(),
            portals = emptyList(),
            environment = UniversalVttDocument.Environment(
                bakedLighting = true,
                ambientLight = AMBIENT_LIGHT,
            ),
            lights = emptyList(),
            image = Base64.getEncoder().encodeToString(originalPng),
        )
    }

    private companion object {
        const val FORMAT_VERSION = 0.2
        const val AMBIENT_LIGHT = "ffffffff"
    }
}
