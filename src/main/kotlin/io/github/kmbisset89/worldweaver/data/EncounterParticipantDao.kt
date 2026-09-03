package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EncounterParticipantDao {
    @Query(
        "SELECT * FROM encounter_participants WHERE encounterId = :encounterId ORDER BY sortIndex ASC"
    )
    suspend fun getByEncounter(encounterId: String): List<EncounterParticipantEntity>

    @Query(
        """
        SELECT participants.* FROM encounter_participants AS participants
        INNER JOIN encounters ON encounters.id = participants.encounterId
        WHERE encounters.campaignId = :campaignId
        ORDER BY participants.encounterId ASC, participants.sortIndex ASC
        """
    )
    fun observeByCampaign(campaignId: String): Flow<List<EncounterParticipantEntity>>

    @Query(
        """
        SELECT participants.* FROM encounter_participants AS participants
        INNER JOIN encounters ON encounters.id = participants.encounterId
        WHERE encounters.campaignId = :campaignId
        ORDER BY participants.encounterId ASC, participants.sortIndex ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<EncounterParticipantEntity>

    @Insert
    suspend fun insertAll(entities: List<EncounterParticipantEntity>)

    @Query("DELETE FROM encounter_participants WHERE encounterId = :encounterId")
    suspend fun deleteByEncounter(encounterId: String)
}
