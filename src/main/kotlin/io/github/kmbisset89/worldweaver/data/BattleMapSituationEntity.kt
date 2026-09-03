package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battle_map_situations",
    foreignKeys = [
        ForeignKey(
            entity = BattleMapEntity::class,
            parentColumns = ["id"],
            childColumns = ["battleMapId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("battleMapId"),
    ],
)
internal data class BattleMapSituationEntity(
    @PrimaryKey val id: String,
    val battleMapId: String,
    val name: String,
    val visible: Boolean,
    val sortIndex: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
