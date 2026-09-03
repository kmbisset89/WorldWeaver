package io.github.kmbisset89.worldweaver.domain

internal data class EncounterParticipant(
    val id: String,
    val name: String,
    val source: EncounterParticipantSource,
    val sourceId: String?,
    val initiativeRoll: Int?,
    val initiativeBonus: Int,
    val armorClass: Int,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val conditions: List<String>,
    val groupCount: Int,
    val combatState: CombatState,
    val gridColumn: Int? = null,
    val gridRow: Int? = null,
    val visibleToPlayers: Boolean = true,
    val attacksAllowed: Int = MIN_ATTACKS_ALLOWED,
    val attacksUsed: Int = 0,
    val bonusActionUsed: Boolean = false,
    val reactionUsed: Boolean = false,
) {
    fun initiativeTotal(): Int? {
        return initiativeRoll?.plus(initiativeBonus)
    }

    fun boardCell(): GridCell? {
        val column = gridColumn ?: return null
        val row = gridRow ?: return null
        return GridCell(column = column, row = row)
    }

    fun hasCondition(condition: FifthEditionCondition): Boolean {
        return conditions.any { label ->
            FifthEditionCondition.fromDisplayName(label) == condition
        }
    }

    fun resetTurnEconomy(): EncounterParticipant {
        return copy(
            attacksUsed = 0,
            bonusActionUsed = false,
            reactionUsed = false,
        )
    }

    companion object {
        const val MIN_ATTACKS_ALLOWED = 1
        const val MAX_ATTACKS_ALLOWED = 8
        const val ATTACKS_ALLOWED_CHIP_MAX = 4
    }
}
