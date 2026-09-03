package net.tactware.worldweaver.domain

/**
 * Builds a copyable image-generation prompt from battle map maker fields.
 */
internal class BattleMapImagePromptFactory {
    fun create(
        name: String,
        columns: Int?,
        rows: Int?,
        unitName: String,
        unitsPerTile: Double?,
        scenery: String,
    ): String {
        return listOf(
            opening(name.trim()),
            gridSentence(columns, rows, unitName.trim(), unitsPerTile),
            STYLE,
            sceneSentence(scenery),
            CLOSING,
        ).joinToString(" ")
    }

    private fun opening(name: String): String {
        return if (name.isEmpty()) {
            "Create a top-down tabletop RPG battle map."
        } else {
            "Create a top-down tabletop RPG battle map for \"$name\"."
        }
    }

    private fun gridSentence(
        columns: Int?,
        rows: Int?,
        unitName: String,
        unitsPerTile: Double?,
    ): String {
        if (columns == null || rows == null || columns < 1 || rows < 1) {
            return "Square grid."
        }
        val scale = scaleClause(unitName, unitsPerTile)
        return if (scale == null) {
            "Square grid, $columns by $rows tiles."
        } else {
            "Square grid, $columns by $rows tiles, $scale."
        }
    }

    private fun scaleClause(unitName: String, unitsPerTile: Double?): String? {
        if (unitsPerTile == null || unitsPerTile <= 0.0) {
            return null
        }
        val amount = formatUnits(unitsPerTile)
        return if (unitName.isEmpty()) {
            "$amount units per square"
        } else {
            "$amount $unitName per square"
        }
    }

    private fun sceneSentence(scenery: String): String {
        val scene = scenery.trim().trimEnd('.').ifBlank { DEFAULT_SCENERY }
        return "Scene: $scene."
    }

    private fun formatUnits(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private companion object {
        const val STYLE =
            "Orthographic overhead view, even lighting, no characters, tokens, compass, UI, or text."
        const val CLOSING =
            "Distinct floors, walls, and obstacles that read clearly at tabletop scale."
        const val DEFAULT_SCENERY =
            "varied terrain with clear walkable space, cover, and landmarks"
    }
}
