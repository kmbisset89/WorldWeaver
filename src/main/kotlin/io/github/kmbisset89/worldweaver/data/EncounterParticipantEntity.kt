package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "encounter_participants",
    foreignKeys = [
        ForeignKey(
            entity = EncounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["encounterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("encounterId")],
)
internal data class EncounterParticipantEntity(
    @PrimaryKey val id: String,
    val encounterId: String,
    val name: String,
    val source: String,
    val sourceId: String?,
    val initiativeRoll: Int?,
    val initiativeBonus: Int,
    val armorClass: Int,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val conditions: String,
    val groupCount: Int,
    val combatState: String,
    val sortIndex: Int,
    val gridColumn: Int? = null,
    val gridRow: Int? = null,
    val visibleToPlayers: Boolean = true,
    val attacksAllowed: Int = 1,
    val attacksUsed: Int = 0,
    val bonusActionUsed: Boolean = false,
    val reactionUsed: Boolean = false,
)
