package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_objectives",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["questId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("questId")],
)
internal data class QuestObjectiveEntity(
    @PrimaryKey val id: String,
    val questId: String,
    val title: String,
    val status: String,
    val sortIndex: Int,
)
