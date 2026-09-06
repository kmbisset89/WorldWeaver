package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SessionClockDao {
    @Query(
        """
        SELECT * FROM session_clocks
        WHERE sessionId = :sessionId
        ORDER BY sortIndex ASC
        """
    )
    fun observeBySession(sessionId: String): Flow<List<SessionClockEntity>>

    @Query("SELECT * FROM session_clocks WHERE id = :id")
    suspend fun getById(id: String): SessionClockEntity?

    @Query(
        """
        SELECT * FROM session_clocks
        WHERE sessionId = :sessionId
        ORDER BY sortIndex ASC
        """
    )
    suspend fun getBySession(sessionId: String): List<SessionClockEntity>

    @Query(
        """
        SELECT clocks.* FROM session_clocks AS clocks
        INNER JOIN sessions ON sessions.id = clocks.sessionId
        WHERE sessions.campaignId = :campaignId
        ORDER BY clocks.sessionId ASC, clocks.sortIndex ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<SessionClockEntity>

    @Insert
    suspend fun insert(entity: SessionClockEntity)

    @Update
    suspend fun update(entity: SessionClockEntity)

    @Query("DELETE FROM session_clocks WHERE id = :id")
    suspend fun delete(id: String)
}
