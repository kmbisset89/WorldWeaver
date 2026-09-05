package io.github.kmbisset89.worldweaver.domain

internal enum class WorldCalendarObservanceKind(
    val displayName: String,
) {
    Holiday("Holiday"),
    ImportantDay("Important day"),
    ;

    companion object {
        fun fromStorage(value: String): WorldCalendarObservanceKind {
            return entries.firstOrNull { it.name == value } ?: Holiday
        }
    }
}
