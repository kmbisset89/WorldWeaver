package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlotThreadDao {
    @Query(
        """
        SELECT * FROM plot_threads
        WHERE campaignId = :campaignId
        ORDER BY priority DESC, title ASC
        """
    )
    fun observeByCampaign(campaignId: String): Flow<List<PlotThreadEntity>>

    @Query("SELECT * FROM plot_threads WHERE id = :id")
    suspend fun getById(id: String): PlotThreadEntity?

    @Query(
        """
        SELECT * FROM plot_threads
        WHERE campaignId = :campaignId
        ORDER BY priority DESC, title ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<PlotThreadEntity>

    @Insert
    suspend fun insert(entity: PlotThreadEntity)

    @Update
    suspend fun update(entity: PlotThreadEntity)

    @Query("DELETE FROM plot_threads WHERE id = :id")
    suspend fun delete(id: String)
}
