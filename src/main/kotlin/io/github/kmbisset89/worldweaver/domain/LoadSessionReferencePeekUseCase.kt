package io.github.kmbisset89.worldweaver.domain

internal class LoadSessionReferencePeekUseCase(
    private val locationRepository: LocationRepository,
    private val locationOverlayRepository: LocationOverlayRepository,
    private val loreRepository: LoreRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
) {
    suspend operator fun invoke(hit: SearchHit, campaignId: String): SessionReferencePeek? {
        return when (hit.kind) {
            SearchKind.Location -> loadLocation(hit, campaignId)
            SearchKind.Lore -> loadLore(hit)
            SearchKind.WorldPerson -> loadWorldPerson(hit)
            SearchKind.CampaignPerson -> loadCampaignPerson(hit)
            else -> null
        }
    }

    private suspend fun loadLocation(hit: SearchHit, campaignId: String): SessionReferencePeek.Location? {
        val location = locationRepository.getById(hit.id) ?: return null
        val overlay = locationOverlayRepository.get(campaignId, location.id)
        return SessionReferencePeek.Location(
            hit = hit,
            title = location.name,
            typeLabel = location.type.displayName,
            description = location.description,
            campaignNotes = overlay?.notes.orEmpty(),
        )
    }

    private suspend fun loadLore(hit: SearchHit): SessionReferencePeek.Lore? {
        val lore = loreRepository.getById(hit.id) ?: return null
        return SessionReferencePeek.Lore(
            hit = hit,
            title = lore.title,
            categoryLabel = lore.category.displayName,
            content = lore.content,
            secrets = lore.secrets.map { secret ->
                SessionReferencePeek.SecretLine(title = secret.title, secret = secret.secret)
            },
        )
    }

    private suspend fun loadWorldPerson(hit: SearchHit): SessionReferencePeek.Person? {
        val person = worldPersonRepository.getById(hit.id) ?: return null
        return SessionReferencePeek.Person(
            hit = hit,
            title = person.name,
            kindLabel = person.kind.displayName,
            description = person.description,
            campaignNotes = "",
        )
    }

    private suspend fun loadCampaignPerson(hit: SearchHit): SessionReferencePeek.Person? {
        val person = campaignPersonRepository.getById(hit.id) ?: return null
        return SessionReferencePeek.Person(
            hit = hit,
            title = person.name,
            kindLabel = person.kind.displayName,
            description = person.description,
            campaignNotes = person.overlayNotes,
        )
    }
}
