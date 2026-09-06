package io.github.kmbisset89.worldweaver.domain

internal sealed class SessionReferencePeek {
    abstract val hit: SearchHit
    abstract val title: String

    data class Location(
        override val hit: SearchHit,
        override val title: String,
        val typeLabel: String,
        val description: String,
        val campaignNotes: String,
    ) : SessionReferencePeek()

    data class Lore(
        override val hit: SearchHit,
        override val title: String,
        val categoryLabel: String,
        val content: String,
        val secrets: List<SecretLine>,
    ) : SessionReferencePeek()

    data class Person(
        override val hit: SearchHit,
        override val title: String,
        val kindLabel: String,
        val description: String,
        val campaignNotes: String,
    ) : SessionReferencePeek()

    data class SecretLine(
        val title: String,
        val secret: String,
    )
}
