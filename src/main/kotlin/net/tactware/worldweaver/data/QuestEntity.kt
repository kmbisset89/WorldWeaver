package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quests",
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
    ],
    indices = [
        Index("campaignId"),
        Index("locationId"),
        Index("status"),
    ],
)
internal data class QuestEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val title: String,
    val summary: String,
    val status: String,
    val locationId: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
