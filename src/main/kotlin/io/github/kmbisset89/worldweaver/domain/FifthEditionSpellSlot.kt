package io.github.kmbisset89.worldweaver.domain

internal data class FifthEditionSpellSlot(
    val level: Int,
    val maximum: Int,
    val used: Int,
) {
    fun remaining(): Int = (maximum - used).coerceAtLeast(0)
}
