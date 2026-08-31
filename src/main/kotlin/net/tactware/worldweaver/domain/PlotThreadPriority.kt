package net.tactware.worldweaver.domain

internal enum class PlotThreadPriority(
    val displayName: String,
    val sortValue: Int,
) {
    Low("Low", 0),
    Medium("Medium", 1),
    High("High", 2),
    Critical("Critical", 3),
    ;

    companion object {
        fun fromStorage(value: String): PlotThreadPriority {
            return entries.firstOrNull { it.name == value } ?: Medium
        }
    }
}
