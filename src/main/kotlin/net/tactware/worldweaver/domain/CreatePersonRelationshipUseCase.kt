package net.tactware.worldweaver.domain

internal class CreatePersonRelationshipUseCase(
    private val personRelationshipRepository: PersonRelationshipRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val campaignRepository: CampaignRepository,
    private val factionRepository: FactionRepository,
    private val entityIdFactory: EntityIdFactory,
) {
    sealed interface Result {
        data class Created(val relationship: PersonRelationship) : Result
        data object InvalidTarget : Result
        data object SelfRelationship : Result
        data object InvalidFaction : Result
        data object WrongWorld : Result
    }

    suspend operator fun invoke(
        from: PersonRef,
        to: PersonRef,
        type: RelationshipType,
        description: String,
        factionId: String?,
    ): Result {
        if (from.id == to.id && from::class == to::class) {
            return Result.SelfRelationship
        }
        if (!personExists(from) || !personExists(to)) {
            return Result.InvalidTarget
        }
        val resolvedFactionId = factionId?.takeIf { it.isNotBlank() }
        if (resolvedFactionId != null) {
            val faction = factionRepository.getById(resolvedFactionId) ?: return Result.InvalidFaction
            val fromWorld = personWorldId(from)
            val toWorld = personWorldId(to)
            if (fromWorld != faction.worldId && toWorld != faction.worldId) {
                return Result.WrongWorld
            }
        }
        val relationship = PersonRelationship(
            id = entityIdFactory.create(),
            from = from,
            to = to,
            type = type,
            description = description.trim(),
            factionId = resolvedFactionId,
        )
        personRelationshipRepository.insert(relationship)
        return Result.Created(relationship)
    }

    private suspend fun personExists(ref: PersonRef): Boolean {
        return when (ref) {
            is PersonRef.World -> worldPersonRepository.getById(ref.id) != null
            is PersonRef.Campaign -> campaignPersonRepository.getById(ref.id) != null
        }
    }

    private suspend fun personWorldId(ref: PersonRef): String? {
        return when (ref) {
            is PersonRef.World -> worldPersonRepository.getById(ref.id)?.worldId
            is PersonRef.Campaign -> {
                val person = campaignPersonRepository.getById(ref.id) ?: return null
                campaignRepository.getById(person.campaignId)?.worldId
            }
        }
    }
}
