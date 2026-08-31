package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reference_docs",
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
internal data class ReferenceDocEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val sessionId: String?,
    val title: String,
    val pathOrUrl: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
