package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldCalendarObservanceDao {
    @Query("SELECT * FROM world_calendar_observances WHERE worldId = :worldId ORDER BY name ASC")
    fun observeByWorld(worldId: String): Flow<List<WorldCalendarObservanceEntity>>

    @Query("SELECT * FROM world_calendar_observances WHERE worldId = :worldId ORDER BY name ASC")
    suspend fun getByWorld(worldId: String): List<WorldCalendarObservanceEntity>

    @Query("SELECT * FROM world_calendar_observances WHERE id = :id")
    suspend fun getById(id: String): WorldCalendarObservanceEntity?

    @Query(
        """
        SELECT * FROM world_calendar_observances
        WHERE name LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<WorldCalendarObservanceEntity>

    @Insert
    suspend fun insert(entity: WorldCalendarObservanceEntity)

    @Update
    suspend fun update(entity: WorldCalendarObservanceEntity)

    @Query("DELETE FROM world_calendar_observances WHERE id = :id")
    suspend fun delete(id: String)
}
