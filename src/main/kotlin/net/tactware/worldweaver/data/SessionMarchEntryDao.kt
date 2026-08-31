package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SessionMarchEntryDao {
    @Query("SELECT * FROM session_march_entries WHERE sessionId = :sessionId ORDER BY sortIndex ASC")
    suspend fun getBySession(sessionId: String): List<SessionMarchEntryEntity>

    @Query(
        """
        SELECT entries.* FROM session_march_entries AS entries
        INNER JOIN sessions ON sessions.id = entries.sessionId
        WHERE sessions.campaignId = :campaignId
        ORDER BY entries.sessionId ASC, entries.sortIndex ASC
        """
    )
    fun observeByCampaign(campaignId: String): Flow<List<SessionMarchEntryEntity>>

    @Query(
        """
        SELECT entries.* FROM session_march_entries AS entries
        INNER JOIN sessions ON sessions.id = entries.sessionId
        WHERE sessions.campaignId = :campaignId
        ORDER BY entries.sessionId ASC, entries.sortIndex ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<SessionMarchEntryEntity>

    @Insert
    suspend fun insertAll(entities: List<SessionMarchEntryEntity>)

    @Query("DELETE FROM session_march_entries WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
