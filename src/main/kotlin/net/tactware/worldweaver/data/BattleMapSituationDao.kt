package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BattleMapSituationDao {
    @Query(
        """
        SELECT situations.* FROM battle_map_situations AS situations
        INNER JOIN battle_maps AS maps ON maps.id = situations.battleMapId
        WHERE maps.campaignId = :campaignId
        ORDER BY situations.battleMapId ASC, situations.sortIndex ASC
        """,
    )
    fun observeByCampaign(campaignId: String): Flow<List<BattleMapSituationEntity>>

    @Query("SELECT * FROM battle_map_situations WHERE id = :id")
    suspend fun getById(id: String): BattleMapSituationEntity?

    @Query(
        """
        SELECT * FROM battle_map_situations
        WHERE battleMapId = :battleMapId
        ORDER BY sortIndex ASC
        """,
    )
    suspend fun getByBattleMap(battleMapId: String): List<BattleMapSituationEntity>

    @Insert
    suspend fun insert(entity: BattleMapSituationEntity)

    @Update
    suspend fun update(entity: BattleMapSituationEntity)

    @Query("DELETE FROM battle_map_situations WHERE id = :id")
    suspend fun delete(id: String)
}
