package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_links",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["questId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("questId"),
        Index("kind"),
        Index("targetId"),
    ],
)
internal data class QuestLinkEntity(
    @PrimaryKey val id: String,
    val questId: String,
    val kind: String,
    val targetId: String,
)
