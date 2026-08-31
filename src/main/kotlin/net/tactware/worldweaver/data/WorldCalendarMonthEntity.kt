package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_calendar_months",
    foreignKeys = [
        ForeignKey(
            entity = WorldCalendarEntity::class,
            parentColumns = ["id"],
            childColumns = ["calendarId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("calendarId")],
)
internal data class WorldCalendarMonthEntity(
    @PrimaryKey val id: String,
    val calendarId: String,
    val name: String,
    val days: Int,
    val sortIndex: Int,
)
