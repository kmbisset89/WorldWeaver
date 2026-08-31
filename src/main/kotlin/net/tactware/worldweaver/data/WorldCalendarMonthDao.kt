package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldCalendarMonthDao {
    @Query("SELECT * FROM world_calendar_months WHERE calendarId = :calendarId ORDER BY sortIndex ASC")
    fun observeByCalendar(calendarId: String): Flow<List<WorldCalendarMonthEntity>>

    @Query("SELECT * FROM world_calendar_months WHERE calendarId = :calendarId ORDER BY sortIndex ASC")
    suspend fun getByCalendar(calendarId: String): List<WorldCalendarMonthEntity>

    @Insert
    suspend fun insertAll(entities: List<WorldCalendarMonthEntity>)

    @Query("DELETE FROM world_calendar_months WHERE calendarId = :calendarId")
    suspend fun deleteByCalendar(calendarId: String)
}
