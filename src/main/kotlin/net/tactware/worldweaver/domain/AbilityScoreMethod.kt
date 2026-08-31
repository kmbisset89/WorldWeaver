package net.tactware.worldweaver.domain

internal enum class AbilityScoreMethod(
    val displayName: String,
) {
    ThreeD6("3d6"),
    FourD6DropLowest("4d6 drop lowest"),
}
