package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plot_threads",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("campaignId"),
        Index("sessionId"),
    ],
)
internal data class PlotThreadEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val sessionId: String?,
    val title: String,
    val details: String,
    val status: String,
    val priority: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
