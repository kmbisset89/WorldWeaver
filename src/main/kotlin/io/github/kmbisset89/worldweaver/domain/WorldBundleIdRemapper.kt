package io.github.kmbisset89.worldweaver.domain

internal class WorldBundleIdRemapper(
    private val entityIdFactory: EntityIdFactory,
) {
    fun remap(bundle: WorldBundle): WorldBundle {
        val worldId = entityIdFactory.create()
        val calendarId = entityIdFactory.create()
        val monthIds = remapIds(bundle.calendar?.months?.map { it.id }.orEmpty())
        val weekdayIds = remapIds(bundle.calendar?.weekdays?.map { it.id }.orEmpty())
        val locationIds = remapIds(bundle.locations.map { it.id })
        val worldPersonIds = remapIds(bundle.worldPeople.map { it.id })
        val loreIds = remapIds(bundle.loreEntries.map { it.id })
        val factionIds = remapIds(bundle.factions.map { it.id })
        val membershipIds = remapIds(bundle.memberships.map { it.id })
        val campaignIds = remapIds(bundle.campaigns.map { it.id })
        val campaignPersonIds = remapIds(bundle.campaignPeople.map { it.id })
        val questIds = remapIds(bundle.quests.map { it.id })
        val sessionIds = remapIds(bundle.sessions.map { it.id })
        val plotThreadIds = remapIds(bundle.plotThreads.map { it.id })
        val referenceDocIds = remapIds(bundle.referenceDocs.map { it.id })
        val battleMapIds = remapIds(bundle.battleMaps.map { it.id })
        val situationIds = remapIds(bundle.battleMapSituations.map { it.id })
        val worldMapIds = remapIds(bundle.worldMaps.map { it.id })
        val encounterIds = remapIds(bundle.encounters.map { it.id })
        val relationshipIds = remapIds(bundle.relationships.map { it.id })
        val companionIds = remapIds(bundle.companions.map { it.id })
        val nowName = importedName(bundle.world.name)
        return WorldBundle(
            formatVersion = bundle.formatVersion,
            exportedAt = bundle.exportedAt,
            world = bundle.world.copy(id = worldId, name = nowName),
            calendar = bundle.calendar?.let { calendar ->
                calendar.copy(
                    id = calendarId,
                    worldId = worldId,
                    months = calendar.months.map { month ->
                        month.copy(id = monthIds.getValue(month.id))
                    },
                    weekdays = calendar.weekdays.map { weekday ->
                        weekday.copy(id = weekdayIds.getValue(weekday.id))
                    },
                    currentDate = remapWorldDate(calendar.currentDate, monthIds),
                )
            },
            campaigns = bundle.campaigns.map { campaign ->
                campaign.copy(
                    id = campaignIds.getValue(campaign.id),
                    worldId = worldId,
                )
            },
            locations = bundle.locations.map { location ->
                location.copy(
                    id = locationIds.getValue(location.id),
                    worldId = worldId,
                    parentLocationId = location.parentLocationId?.let(locationIds::get),
                )
            },
            loreEntries = bundle.loreEntries.map { lore ->
                lore.copy(
                    id = loreIds.getValue(lore.id),
                    worldId = worldId,
                    relatedEntryIds = lore.relatedEntryIds.mapNotNull(loreIds::get),
                    secrets = lore.secrets.map { secret ->
                        secret.copy(
                            id = entityIdFactory.create(),
                            hints = secret.hints.map { hint -> hint.copy(id = entityIdFactory.create()) },
                        )
                    },
                    locationId = lore.locationId?.let(locationIds::get),
                    characterId = remapCharacterId(lore.characterId, worldPersonIds, campaignPersonIds),
                )
            },
            factions = bundle.factions.map { faction ->
                faction.copy(
                    id = factionIds.getValue(faction.id),
                    worldId = worldId,
                )
            },
            memberships = bundle.memberships.map { membership ->
                membership.copy(
                    id = membershipIds.getValue(membership.id),
                    person = remapPersonRef(membership.person, worldPersonIds, campaignPersonIds),
                    factionId = factionIds[membership.factionId] ?: membership.factionId,
                )
            },
            worldPeople = bundle.worldPeople.map { person ->
                person.copy(
                    id = worldPersonIds.getValue(person.id),
                    worldId = worldId,
                )
            },
            campaignPeople = bundle.campaignPeople.map { person ->
                person.copy(
                    id = campaignPersonIds.getValue(person.id),
                    campaignId = campaignIds.getValue(person.campaignId),
                    worldPersonId = person.worldPersonId?.let(worldPersonIds::get),
                )
            },
            locationOverlays = bundle.locationOverlays.map { overlay ->
                overlay.copy(
                    campaignId = campaignIds.getValue(overlay.campaignId),
                    locationId = locationIds.getValue(overlay.locationId),
                )
            },
            quests = bundle.quests.map { quest ->
                quest.copy(
                    id = questIds.getValue(quest.id),
                    campaignId = campaignIds.getValue(quest.campaignId),
                    locationId = quest.locationId?.let(locationIds::get),
                    objectives = quest.objectives.map { objective ->
                        objective.copy(id = entityIdFactory.create())
                    },
                    links = quest.links.map { link ->
                        link.copy(
                            id = entityIdFactory.create(),
                            targetId = remapQuestTarget(link, loreIds, worldPersonIds, campaignPersonIds, sessionIds),
                        )
                    },
                )
            },
            sessions = bundle.sessions.map { session ->
                session.copy(
                    id = sessionIds.getValue(session.id),
                    campaignId = campaignIds.getValue(session.campaignId),
                    inWorldDate = remapWorldDate(session.inWorldDate, monthIds),
                    scenes = session.scenes.map { scene -> scene.copy(id = entityIdFactory.create()) },
                    marchOrder = session.marchOrder.map { entry ->
                        entry.copy(
                            id = entityIdFactory.create(),
                            person = remapPersonRef(entry.person, worldPersonIds, campaignPersonIds),
                        )
                    },
                )
            },
            plotThreads = bundle.plotThreads.map { thread ->
                thread.copy(
                    id = plotThreadIds.getValue(thread.id),
                    campaignId = campaignIds.getValue(thread.campaignId),
                    sessionId = thread.sessionId?.let(sessionIds::get),
                )
            },
            referenceDocs = bundle.referenceDocs.map { doc ->
                doc.copy(
                    id = referenceDocIds.getValue(doc.id),
                    campaignId = campaignIds.getValue(doc.campaignId),
                    sessionId = doc.sessionId?.let(sessionIds::get),
                )
            },
            battleMaps = bundle.battleMaps.map { map ->
                map.copy(
                    id = battleMapIds.getValue(map.id),
                    campaignId = campaignIds.getValue(map.campaignId),
                    items = map.items.map { item ->
                        item.copy(id = entityIdFactory.create())
                    },
                )
            },
            battleMapSituations = bundle.battleMapSituations.map { situation ->
                situation.copy(
                    id = situationIds.getValue(situation.id),
                    battleMapId = battleMapIds.getValue(situation.battleMapId),
                )
            },
            worldMaps = bundle.worldMaps.map { map ->
                map.copy(
                    id = worldMapIds.getValue(map.id),
                    worldId = worldId,
                    locationId = map.locationId?.let(locationIds::get),
                )
            },
            encounters = bundle.encounters.map { encounter ->
                encounter.copy(
                    id = encounterIds.getValue(encounter.id),
                    campaignId = campaignIds.getValue(encounter.campaignId),
                    locationId = encounter.locationId?.let(locationIds::get),
                    battleMapId = encounter.battleMapId?.let(battleMapIds::get),
                    participants = encounter.participants.map { participant ->
                        participant.copy(
                            id = entityIdFactory.create(),
                            sourceId = remapParticipantSource(
                                source = participant.source,
                                sourceId = participant.sourceId,
                                worldPersonIds = worldPersonIds,
                                campaignPersonIds = campaignPersonIds,
                            ),
                        )
                    },
                )
            },
            relationships = bundle.relationships.map { relationship ->
                relationship.copy(
                    id = relationshipIds.getValue(relationship.id),
                    from = remapPersonRef(relationship.from, worldPersonIds, campaignPersonIds),
                    to = remapPersonRef(relationship.to, worldPersonIds, campaignPersonIds),
                    factionId = relationship.factionId?.let { factionIds[it] ?: it },
                )
            },
            companions = bundle.companions.map { companion ->
                companion.copy(
                    id = companionIds.getValue(companion.id),
                    owner = remapPersonRef(companion.owner, worldPersonIds, campaignPersonIds),
                    companion = remapPersonRef(companion.companion, worldPersonIds, campaignPersonIds),
                )
            },
            avatarFiles = bundle.avatarFiles.map { file ->
                file.copy(ref = remapPersonRef(file.ref, worldPersonIds, campaignPersonIds))
            },
            mapFiles = bundle.mapFiles.map { file ->
                file.copy(
                    battleMapId = battleMapIds.getValue(file.battleMapId),
                    relativePath = remapMapRelativePath(file.relativePath, situationIds),
                )
            },
            worldMapFiles = bundle.worldMapFiles.map { file ->
                file.copy(worldMapId = worldMapIds.getValue(file.worldMapId))
            },
            voiceFiles = bundle.voiceFiles.map { file ->
                file.copy(
                    ref = remapVoiceRef(file.ref, locationIds, worldPersonIds, campaignPersonIds),
                )
            },
        )
    }

    private fun remapWorldDate(
        date: WorldDate?,
        monthIds: Map<String, String>,
    ): WorldDate? {
        if (date == null) {
            return null
        }
        return date.copy(monthId = monthIds[date.monthId] ?: date.monthId)
    }

    private fun remapIds(ids: List<String>): Map<String, String> {
        return ids.associateWith { entityIdFactory.create() }
    }

    private fun importedName(name: String): String {
        return if (name.endsWith(IMPORTED_SUFFIX)) {
            name
        } else {
            "$name$IMPORTED_SUFFIX"
        }
    }

    private fun remapCharacterId(
        characterId: String?,
        worldPersonIds: Map<String, String>,
        campaignPersonIds: Map<String, String>,
    ): String? {
        if (characterId == null) {
            return null
        }
        return worldPersonIds[characterId] ?: campaignPersonIds[characterId]
    }

    private fun remapQuestTarget(
        link: QuestLink,
        loreIds: Map<String, String>,
        worldPersonIds: Map<String, String>,
        campaignPersonIds: Map<String, String>,
        sessionIds: Map<String, String>,
    ): String {
        val remapped = when (link.kind) {
            QuestLinkKind.LORE -> loreIds[link.targetId]
            QuestLinkKind.WORLD_PERSON -> worldPersonIds[link.targetId]
            QuestLinkKind.CAMPAIGN_PERSON -> campaignPersonIds[link.targetId]
            QuestLinkKind.SESSION -> sessionIds[link.targetId]
        }
        return remapped ?: link.targetId
    }

    private fun remapVoiceRef(
        ref: VoiceClipRef,
        locationIds: Map<String, String>,
        worldPersonIds: Map<String, String>,
        campaignPersonIds: Map<String, String>,
    ): VoiceClipRef {
        return when (ref) {
            is VoiceClipRef.Location -> VoiceClipRef.Location(locationIds[ref.id] ?: ref.id)
            is VoiceClipRef.WorldPerson -> VoiceClipRef.WorldPerson(worldPersonIds[ref.id] ?: ref.id)
            is VoiceClipRef.CampaignPerson -> {
                VoiceClipRef.CampaignPerson(campaignPersonIds[ref.id] ?: ref.id)
            }
        }
    }

    private fun remapPersonRef(
        ref: PersonRef,
        worldPersonIds: Map<String, String>,
        campaignPersonIds: Map<String, String>,
    ): PersonRef {
        return when (ref) {
            is PersonRef.World -> PersonRef.World(worldPersonIds[ref.id] ?: ref.id)
            is PersonRef.Campaign -> PersonRef.Campaign(campaignPersonIds[ref.id] ?: ref.id)
        }
    }

    private fun remapParticipantSource(
        source: EncounterParticipantSource,
        sourceId: String?,
        worldPersonIds: Map<String, String>,
        campaignPersonIds: Map<String, String>,
    ): String? {
        if (sourceId == null) {
            return null
        }
        return when (source) {
            EncounterParticipantSource.WorldPerson -> worldPersonIds[sourceId] ?: sourceId
            EncounterParticipantSource.CampaignPerson -> campaignPersonIds[sourceId] ?: sourceId
            EncounterParticipantSource.Nameless -> sourceId
        }
    }

    private fun remapMapRelativePath(
        relativePath: String,
        situationIds: Map<String, String>,
    ): String {
        val prefix = "situations/"
        if (!relativePath.startsWith(prefix)) {
            return relativePath
        }
        val remainder = relativePath.removePrefix(prefix)
        val slash = remainder.indexOf('/')
        if (slash <= 0) {
            return relativePath
        }
        val oldSituationId = remainder.substring(0, slash)
        val newSituationId = situationIds[oldSituationId] ?: oldSituationId
        return prefix + newSituationId + remainder.substring(slash)
    }

    private companion object {
        const val IMPORTED_SUFFIX = " (imported)"
    }
}
