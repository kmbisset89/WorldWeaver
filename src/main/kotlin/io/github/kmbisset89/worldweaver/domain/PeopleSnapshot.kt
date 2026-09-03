package io.github.kmbisset89.worldweaver.domain

internal data class PeopleSnapshot(
    val worldPeople: List<WorldPerson>,
    val campaignPeople: List<CampaignPerson>,
)
