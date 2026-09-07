package io.github.kmbisset89.worldweaver.domain

internal class WikilinkTextParser {
    fun parse(text: String): List<WikilinkSegment> {
        if (text.isEmpty()) {
            return emptyList()
        }
        val segments = mutableListOf<WikilinkSegment>()
        var cursor = 0
        LINK_PATTERN.findAll(text).forEach { match ->
            if (match.range.first > cursor) {
                segments += WikilinkSegment.Text(text.substring(cursor, match.range.first))
            }
            val inner = match.groupValues[1]
            segments += parseLink(raw = match.value, inner = inner)
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            segments += WikilinkSegment.Text(text.substring(cursor))
        }
        return segments
    }

    fun incompleteQuery(text: String): String? {
        val lastOpen = text.lastIndexOf("[[")
        if (lastOpen < 0) {
            return null
        }
        val after = text.substring(lastOpen + 2)
        if (after.contains("]]") || after.contains('\n')) {
            return null
        }
        return after
    }

    private fun parseLink(raw: String, inner: String): WikilinkSegment.Link {
        val separator = inner.indexOf('|')
        val lookup = if (separator >= 0) {
            inner.take(separator).trim()
        } else {
            inner.trim()
        }
        val alias = if (separator >= 0) {
            inner.substring(separator + 1).trim().ifEmpty { null }
        } else {
            null
        }
        val colon = lookup.indexOf(':')
        val prefix = if (colon > 0) lookup.take(colon).trim() else null
        val kind = prefix?.let { WikilinkTargetKind.fromPrefix(it) }
        val targetId = if (kind != null) {
            lookup.substring(colon + 1).trim().ifEmpty { null }
        } else {
            null
        }
        return WikilinkSegment.Link(
            raw = raw,
            inner = inner,
            lookup = lookup,
            alias = alias,
            kindPrefix = kind?.tokenPrefix,
            targetId = targetId,
        )
    }

    private companion object {
        val LINK_PATTERN = Regex("""\[\[([^\[\]]+?)]]""")
    }
}
