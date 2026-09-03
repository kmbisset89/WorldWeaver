package io.github.kmbisset89.worldweaver.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorldEntity::class,
        WorldCalendarEntity::class,
        WorldCalendarMonthEntity::class,
        WorldCalendarWeekdayEntity::class,
        CampaignEntity::class,
        LocationEntity::class,
        LocationOverlayEntity::class,
        LoreEntity::class,
        LoreSecretEntity::class,
        LoreHintEntity::class,
        WorldPersonEntity::class,
        CampaignPersonEntity::class,
        FactionEntity::class,
        FactionMembershipEntity::class,
        PersonRelationshipEntity::class,
        PersonCompanionEntity::class,
        QuestEntity::class,
        QuestObjectiveEntity::class,
        QuestLinkEntity::class,
        SessionEntity::class,
        SessionSceneEntity::class,
        SessionMarchEntryEntity::class,
        PlotThreadEntity::class,
        ReferenceDocEntity::class,
        EncounterEntity::class,
        EncounterParticipantEntity::class,
        BattleMapEntity::class,
        BattleMapSituationEntity::class,
    ],
    version = 20,
    exportSchema = true,
)
internal abstract class WorldWeaverDatabase : RoomDatabase() {
    abstract fun worldDao(): WorldDao
    abstract fun worldCalendarDao(): WorldCalendarDao
    abstract fun worldCalendarMonthDao(): WorldCalendarMonthDao
    abstract fun worldCalendarWeekdayDao(): WorldCalendarWeekdayDao
    abstract fun campaignDao(): CampaignDao
    abstract fun locationDao(): LocationDao
    abstract fun locationOverlayDao(): LocationOverlayDao
    abstract fun loreDao(): LoreDao
    abstract fun loreSecretDao(): LoreSecretDao
    abstract fun loreHintDao(): LoreHintDao
    abstract fun worldPersonDao(): WorldPersonDao
    abstract fun campaignPersonDao(): CampaignPersonDao
    abstract fun factionDao(): FactionDao
    abstract fun factionMembershipDao(): FactionMembershipDao
    abstract fun personRelationshipDao(): PersonRelationshipDao
    abstract fun personCompanionDao(): PersonCompanionDao
    abstract fun questDao(): QuestDao
    abstract fun questObjectiveDao(): QuestObjectiveDao
    abstract fun questLinkDao(): QuestLinkDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionSceneDao(): SessionSceneDao
    abstract fun sessionMarchEntryDao(): SessionMarchEntryDao
    abstract fun plotThreadDao(): PlotThreadDao
    abstract fun referenceDocDao(): ReferenceDocDao
    abstract fun encounterDao(): EncounterDao
    abstract fun encounterParticipantDao(): EncounterParticipantDao
    abstract fun battleMapDao(): BattleMapDao
    abstract fun battleMapSituationDao(): BattleMapSituationDao
}
