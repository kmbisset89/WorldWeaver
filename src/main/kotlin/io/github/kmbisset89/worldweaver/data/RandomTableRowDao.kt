package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RandomTableRowDao {
    @Query("SELECT * FROM random_table_rows WHERE tableId = :tableId ORDER BY sortIndex ASC")
    suspend fun getByTable(tableId: String): List<RandomTableRowEntity>

    @Query(
        """
        SELECT random_table_rows.* FROM random_table_rows
        INNER JOIN random_tables ON random_tables.id = random_table_rows.tableId
        WHERE random_tables.worldId = :worldId
        ORDER BY random_table_rows.tableId ASC, random_table_rows.sortIndex ASC
        """
    )
    fun observeByWorld(worldId: String): Flow<List<RandomTableRowEntity>>

    @Query(
        """
        SELECT random_table_rows.* FROM random_table_rows
        INNER JOIN random_tables ON random_tables.id = random_table_rows.tableId
        WHERE random_tables.worldId = :worldId
        ORDER BY random_table_rows.tableId ASC, random_table_rows.sortIndex ASC
        """
    )
    suspend fun getByWorld(worldId: String): List<RandomTableRowEntity>

    @Insert
    suspend fun insertAll(entities: List<RandomTableRowEntity>)

    @Query("DELETE FROM random_table_rows WHERE tableId = :tableId")
    suspend fun deleteByTable(tableId: String)
}
