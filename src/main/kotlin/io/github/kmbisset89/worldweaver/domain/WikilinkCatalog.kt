package io.github.kmbisset89.worldweaver.domain

internal class WikilinkCatalog(
    targets: List<WikilinkTarget>,
) {
    private val targets = targets
    private val byId = targets.associateBy { target -> target.id }
    private val byKindAndId = targets.associateBy { target ->
        target.kind.tokenPrefix to target.id
    }

    fun targetById(id: String): WikilinkTarget? = byId[id]

    fun resolve(link: WikilinkSegment.Link): WikilinkTarget? {
        val prefixedId = link.targetId
        val prefix = link.kindPrefix
        if (prefix != null && prefixedId != null) {
            return byKindAndId[prefix to prefixedId] ?: byId[prefixedId]
        }
        val title = link.lookup.trim()
        if (title.isEmpty()) {
            return null
        }
        val matches = targets.filter { target ->
            target.title.equals(title, ignoreCase = true)
        }
        return when {
            matches.size == 1 -> matches.first()
            matches.size > 1 -> matches.firstOrNull { target ->
                target.title == title
            }
            else -> null
        }
    }

    fun suggest(query: String, limit: Int = SUGGESTION_LIMIT): List<WikilinkTarget> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return targets.sortedBy { it.title.lowercase() }.take(limit)
        }
        val colon = trimmed.indexOf(':')
        val prefixKind = if (colon > 0) {
            WikilinkTargetKind.fromPrefix(trimmed.take(colon).trim())
        } else {
            null
        }
        val needle = if (prefixKind != null) {
            trimmed.substring(colon + 1).trim()
        } else {
            trimmed
        }
        val lower = needle.lowercase()
        return targets
            .filter { target ->
                val kindMatches = prefixKind == null || target.kind == prefixKind
                val titleMatches = lower.isEmpty() ||
                    target.title.lowercase().contains(lower)
                kindMatches && titleMatches
            }
            .sortedWith(
                compareByDescending<WikilinkTarget> { target ->
                    target.title.lowercase().startsWith(lower)
                }.thenBy { target -> target.title.lowercase() },
            )
            .take(limit)
    }

    private companion object {
        const val SUGGESTION_LIMIT = 8
    }
}
