package net.tactware.worldweaver.domain

internal data class PeopleSnapshot(
    val worldPeople: List<WorldPerson>,
    val campaignPeople: List<CampaignPerson>,
)
