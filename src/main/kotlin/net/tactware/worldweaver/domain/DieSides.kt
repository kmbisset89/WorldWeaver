package net.tactware.worldweaver.domain

internal enum class DieSides(
    val sides: Int,
) {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20),
    D100(100),
    ;

    val label: String
        get() = "d$sides"

    companion object {
        fun fromSides(sides: Int): DieSides? {
            return entries.firstOrNull { it.sides == sides }
        }
    }
}
