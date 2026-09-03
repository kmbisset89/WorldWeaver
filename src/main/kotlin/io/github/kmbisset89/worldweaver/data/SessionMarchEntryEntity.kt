package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_march_entries",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
internal data class SessionMarchEntryEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val personScope: String,
    val personId: String,
    val displayName: String,
    val sortIndex: Int,
)
