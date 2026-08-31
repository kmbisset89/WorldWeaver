package net.tactware.worldweaver.domain

internal class CreatePersonRelationshipUseCase(
    private val personRelationshipRepository: PersonRelationshipRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val entityIdFactory: EntityIdFactory,
) {
    sealed interface Result {
        data class Created(val relationship: PersonRelationship) : Result
        data object InvalidTarget : Result
        data object SelfRelationship : Result
    }

    suspend operator fun invoke(
        from: PersonRef,
        to: PersonRef,
        type: RelationshipType,
        description: String,
        factionLean: String,
    ): Result {
        if (from.id == to.id && from::class == to::class) {
            return Result.SelfRelationship
        }
        if (!personExists(from) || !personExists(to)) {
            return Result.InvalidTarget
        }
        val relationship = PersonRelationship(
            id = entityIdFactory.create(),
            from = from,
            to = to,
            type = type,
            description = description.trim(),
            factionLean = factionLean.trim(),
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
}
