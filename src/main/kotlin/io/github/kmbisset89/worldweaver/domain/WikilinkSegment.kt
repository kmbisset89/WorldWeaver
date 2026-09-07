package io.github.kmbisset89.worldweaver.domain

internal sealed interface WikilinkSegment {
    data class Text(
        val value: String,
    ) : WikilinkSegment

    data class Link(
        val raw: String,
        val inner: String,
        val lookup: String,
        val alias: String?,
        val kindPrefix: String?,
        val targetId: String?,
    ) : WikilinkSegment {
        val displayText: String
            get() = alias?.ifBlank { lookup } ?: lookup
    }
}
