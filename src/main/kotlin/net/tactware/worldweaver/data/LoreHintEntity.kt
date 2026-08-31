package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lore_hints",
    foreignKeys = [
        ForeignKey(
            entity = LoreSecretEntity::class,
            parentColumns = ["id"],
            childColumns = ["secretId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("secretId")],
)
internal data class LoreHintEntity(
    @PrimaryKey val id: String,
    val secretId: String,
    val text: String,
    val revealed: Boolean,
    val sortIndex: Int,
)
