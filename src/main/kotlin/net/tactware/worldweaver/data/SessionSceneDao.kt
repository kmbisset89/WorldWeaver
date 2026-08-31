package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SessionSceneDao {
    @Query("SELECT * FROM session_scenes WHERE sessionId = :sessionId ORDER BY sortIndex ASC")
    suspend fun getBySession(sessionId: String): List<SessionSceneEntity>

    @Query(
        """
        SELECT scenes.* FROM session_scenes AS scenes
        INNER JOIN sessions ON sessions.id = scenes.sessionId
        WHERE sessions.campaignId = :campaignId
        ORDER BY scenes.sessionId ASC, scenes.sortIndex ASC
        """
    )
    fun observeByCampaign(campaignId: String): Flow<List<SessionSceneEntity>>

    @Query(
        """
        SELECT scenes.* FROM session_scenes AS scenes
        INNER JOIN sessions ON sessions.id = scenes.sessionId
        WHERE sessions.campaignId = :campaignId
        ORDER BY scenes.sessionId ASC, scenes.sortIndex ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<SessionSceneEntity>

    @Insert
    suspend fun insertAll(entities: List<SessionSceneEntity>)

    @Query("DELETE FROM session_scenes WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
