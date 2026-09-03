package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("campaignId")],
)
internal data class SessionEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val name: String,
    val notes: String,
    val inWorldYear: Int?,
    val inWorldMonthId: String?,
    val inWorldDay: Int?,
    val recap: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
