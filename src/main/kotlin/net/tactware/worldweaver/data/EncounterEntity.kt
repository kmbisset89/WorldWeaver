package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "encounters",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = BattleMapEntity::class,
            parentColumns = ["id"],
            childColumns = ["battleMapId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("campaignId"),
        Index("locationId"),
        Index("status"),
        Index("battleMapId"),
    ],
)
internal data class EncounterEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val name: String,
    val locationId: String?,
    val battleMapId: String?,
    val difficulty: String,
    val notes: String,
    val outcomeNote: String,
    val status: String,
    val currentRound: Int,
    val currentTurnIndex: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
