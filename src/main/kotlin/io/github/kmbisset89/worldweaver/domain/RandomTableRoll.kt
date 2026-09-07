package io.github.kmbisset89.worldweaver.domain

internal data class RandomTableRoll(
    val tableId: String,
    val tableName: String,
    val label: String,
    val nested: RandomTableRoll?,
) {
    fun displayText(): String {
        return if (nested == null) {
            label
        } else {
            "$label → ${nested.displayText()}"
        }
    }
}
