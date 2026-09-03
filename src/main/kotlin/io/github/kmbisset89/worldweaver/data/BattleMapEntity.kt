package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battle_maps",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("campaignId"),
    ],
)
internal data class BattleMapEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val name: String,
    val originalWidth: Int,
    val originalHeight: Int,
    val tileSizePx: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val columns: Int,
    val rows: Int,
    val unitName: String,
    val unitsPerTile: Double,
    val fogEnabled: Boolean = false,
    val revealedCells: String = "",
    val blockedCells: String = "",
    val difficultCells: String = "",
    val items: String = "",
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
