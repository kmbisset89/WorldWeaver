package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldCalendarDao {
    @Query("SELECT * FROM world_calendars WHERE worldId = :worldId LIMIT 1")
    fun observeByWorld(worldId: String): Flow<WorldCalendarEntity?>

    @Query("SELECT * FROM world_calendars WHERE worldId = :worldId LIMIT 1")
    suspend fun getByWorld(worldId: String): WorldCalendarEntity?

    @Query("SELECT * FROM world_calendars WHERE id = :id")
    suspend fun getById(id: String): WorldCalendarEntity?

    @Insert
    suspend fun insert(entity: WorldCalendarEntity)

    @Update
    suspend fun update(entity: WorldCalendarEntity)
}
