package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldCalendarObservanceLoreLinkDao {
    @Query("SELECT * FROM world_calendar_observance_lore WHERE observanceId = :observanceId")
    suspend fun getByObservance(observanceId: String): List<WorldCalendarObservanceLoreLinkEntity>

    @Query(
        """
        SELECT links.* FROM world_calendar_observance_lore AS links
        INNER JOIN world_calendar_observances AS observances ON observances.id = links.observanceId
        WHERE observances.worldId = :worldId
        """
    )
    fun observeByWorld(worldId: String): Flow<List<WorldCalendarObservanceLoreLinkEntity>>

    @Query(
        """
        SELECT links.* FROM world_calendar_observance_lore AS links
        INNER JOIN world_calendar_observances AS observances ON observances.id = links.observanceId
        WHERE observances.worldId = :worldId
        """
    )
    suspend fun getByWorld(worldId: String): List<WorldCalendarObservanceLoreLinkEntity>

    @Insert
    suspend fun insertAll(entities: List<WorldCalendarObservanceLoreLinkEntity>)

    @Query("DELETE FROM world_calendar_observance_lore WHERE observanceId = :observanceId")
    suspend fun deleteByObservance(observanceId: String)
}
