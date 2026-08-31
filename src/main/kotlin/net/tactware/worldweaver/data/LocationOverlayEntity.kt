package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "location_overlays",
    primaryKeys = ["campaignId", "locationId"],
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
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("campaignId"),
        Index("locationId"),
    ],
)
internal data class LocationOverlayEntity(
    val campaignId: String,
    val locationId: String,
    val hasPartyPresence: Boolean,
    val notes: String,
    val updatedAtEpochMillis: Long,
)
