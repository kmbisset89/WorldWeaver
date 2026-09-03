package io.github.kmbisset89.worldweaver.domain

internal enum class PlotThreadStatus(
    val displayName: String,
) {
    Open("Open"),
    InProgress("In progress"),
    Resolved("Resolved"),
    Dropped("Dropped"),
    ;

    companion object {
        fun fromStorage(value: String): PlotThreadStatus {
            return entries.firstOrNull { it.name == value } ?: Open
        }
    }
}
