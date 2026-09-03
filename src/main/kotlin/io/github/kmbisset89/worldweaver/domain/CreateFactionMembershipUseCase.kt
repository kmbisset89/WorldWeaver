package io.github.kmbisset89.worldweaver.domain

internal class CreateFactionMembershipUseCase(
    private val factionMembershipRepository: FactionMembershipRepository,
    private val factionRepository: FactionRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val campaignRepository: CampaignRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val membership: FactionMembership) : Result
        data object InvalidPerson : Result
        data object InvalidFaction : Result
        data object WrongWorld : Result
        data object Duplicate : Result
    }

    suspend operator fun invoke(
        person: PersonRef,
        factionId: String,
        role: String,
        notes: String,
    ): Result {
        val faction = factionRepository.getById(factionId) ?: return Result.InvalidFaction
        val worldId = personWorldId(person) ?: return Result.InvalidPerson
        if (worldId != faction.worldId) {
            return Result.WrongWorld
        }
        val alreadyMember = factionMembershipRepository.getByPerson(person).any { membership ->
            membership.factionId == factionId
        }
        if (alreadyMember) {
            return Result.Duplicate
        }
        val membership = FactionMembership(
            id = entityIdFactory.create(),
            person = person,
            factionId = factionId,
            role = role.trim(),
            notes = notes.trim(),
            createdAt = instantProvider.now(),
        )
        factionMembershipRepository.insert(membership)
        return Result.Created(membership)
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
