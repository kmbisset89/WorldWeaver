package net.tactware.worldweaver.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorldEntity::class,
        CampaignEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class WorldWeaverDatabase : RoomDatabase() {
    abstract fun worldDao(): WorldDao
    abstract fun campaignDao(): CampaignDao
}
