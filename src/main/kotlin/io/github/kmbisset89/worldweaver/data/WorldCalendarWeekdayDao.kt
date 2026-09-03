package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldCalendarWeekdayDao {
    @Query("SELECT * FROM world_calendar_weekdays WHERE calendarId = :calendarId ORDER BY sortIndex ASC")
    fun observeByCalendar(calendarId: String): Flow<List<WorldCalendarWeekdayEntity>>

    @Query("SELECT * FROM world_calendar_weekdays WHERE calendarId = :calendarId ORDER BY sortIndex ASC")
    suspend fun getByCalendar(calendarId: String): List<WorldCalendarWeekdayEntity>

    @Insert
    suspend fun insertAll(entities: List<WorldCalendarWeekdayEntity>)

    @Query("DELETE FROM world_calendar_weekdays WHERE calendarId = :calendarId")
    suspend fun deleteByCalendar(calendarId: String)
}
