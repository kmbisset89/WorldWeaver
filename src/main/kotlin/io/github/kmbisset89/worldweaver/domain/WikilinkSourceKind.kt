package io.github.kmbisset89.worldweaver.domain

internal enum class WikilinkSourceKind(
    val label: String,
) {
    Lore("Lore"),
    SessionNotes("Session notes"),
    SessionScratch("Scratch pad"),
    SessionRecap("Recap"),
    SessionScene("Scene"),
}
