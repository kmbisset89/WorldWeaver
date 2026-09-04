package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.Campaign
import io.github.kmbisset89.worldweaver.domain.CampaignStatus
import io.github.kmbisset89.worldweaver.domain.GameSystem
import io.github.kmbisset89.worldweaver.domain.LevelingMode
import java.time.Instant

internal class CampaignEntityConverter {
    fun toCampaign(entity: CampaignEntity): Campaign {
        return Campaign(
            id = entity.id,
            worldId = entity.worldId,
            name = entity.name,
            description = entity.description,
            notes = entity.notes,
            gameSystem = entity.gameSystem?.let { GameSystem.fromStorage(it) },
            levelingMode = LevelingMode.fromStorage(entity.levelingMode),
            status = CampaignStatus.fromStorage(entity.status),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(campaign: Campaign): CampaignEntity {
        return CampaignEntity(
            id = campaign.id,
            worldId = campaign.worldId,
            name = campaign.name,
            description = campaign.description,
            notes = campaign.notes,
            gameSystem = campaign.gameSystem?.name,
            levelingMode = campaign.levelingMode.name,
            status = campaign.status.name,
            createdAtEpochMillis = campaign.createdAt.toEpochMilli(),
            updatedAtEpochMillis = campaign.updatedAt.toEpochMilli(),
        )
    }
}
