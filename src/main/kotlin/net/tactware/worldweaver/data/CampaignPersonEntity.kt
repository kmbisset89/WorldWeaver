package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "campaign_people",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorldPersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["worldPersonId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("campaignId"),
        Index("worldPersonId"),
    ],
)
internal data class CampaignPersonEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val worldPersonId: String?,
    val kind: String,
    val name: String,
    val description: String,
    val race: String,
    val classLevels: String,
    val abilities: String,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val armorClass: Int,
    val walkSpeed: Int,
    val deathSaves: String,
    val items: String,
    val features: String,
    val spells: String,
    val notes: String,
    val overlayHitPoints: Int?,
    val overlayNotes: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
