package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.Campaign
import net.tactware.worldweaver.domain.CampaignStatus
import net.tactware.worldweaver.domain.GameSystem
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
            status = campaign.status.name,
            createdAtEpochMillis = campaign.createdAt.toEpochMilli(),
            updatedAtEpochMillis = campaign.updatedAt.toEpochMilli(),
        )
    }
}
