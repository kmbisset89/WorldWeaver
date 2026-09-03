package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class RelationshipWebFactoryTest {
    private val factory = RelationshipWebFactory()
    private val now = Instant.parse("2026-08-31T12:00:00Z")

    @Test
    fun createIncludesMembershipAndRelationshipEdges() {
        val bram = worldPerson("bram", "Bram")
        val cora = worldPerson("cora", "Cora")
        val harpers = faction("harpers", "Harpers")
        val web = factory.create(
            people = PeopleSnapshot(worldPeople = listOf(bram, cora), campaignPeople = emptyList()),
            factions = listOf(harpers),
            relationships = listOf(
                relationship(
                    id = "rel-1",
                    from = PersonRef.World(bram.id),
                    to = PersonRef.World(cora.id),
                    type = RelationshipType.Ally,
                    factionId = harpers.id,
                )
            ),
            memberships = listOf(
                membership(
                    id = "mem-1",
                    person = PersonRef.World(bram.id),
                    factionId = harpers.id,
                    role = "Scout",
                )
            ),
        )

        assertEquals(3, web.nodes.size)
        assertEquals(2, web.edges.size)
        val relationship = assertIs<RelationshipWeb.Edge.Relationship>(
            web.edges.first { it.id == "rel-1" }
        )
        assertEquals(factory.personNodeId(PersonRef.World(bram.id)), relationship.fromId)
        assertEquals(factory.personNodeId(PersonRef.World(cora.id)), relationship.toId)
        assertEquals(RelationshipType.Ally, relationship.type)
        assertEquals("Harpers", relationship.factionLeanName)
        val membership = assertIs<RelationshipWeb.Edge.Membership>(
            web.edges.first { it.id == "mem-1" }
        )
        assertEquals(factory.personNodeId(PersonRef.World(bram.id)), membership.fromId)
        assertEquals(factory.factionNodeId(harpers.id), membership.toId)
        assertEquals("Scout", membership.role)
    }

    @Test
    fun createDropsDanglingRelationshipAndMembership() {
        val bram = worldPerson("bram", "Bram")
        val harpers = faction("harpers", "Harpers")
        val web = factory.create(
            people = PeopleSnapshot(worldPeople = listOf(bram), campaignPeople = emptyList()),
            factions = listOf(harpers),
            relationships = listOf(
                relationship(
                    id = "rel-missing",
                    from = PersonRef.World(bram.id),
                    to = PersonRef.World("missing"),
                    type = RelationshipType.Rival,
                )
            ),
            memberships = listOf(
                membership(
                    id = "mem-missing-person",
                    person = PersonRef.Campaign("other-campaign"),
                    factionId = harpers.id,
                ),
                membership(
                    id = "mem-missing-faction",
                    person = PersonRef.World(bram.id),
                    factionId = "gone",
                ),
            ),
        )

        assertTrue(web.edges.isEmpty())
        assertEquals(2, web.nodes.size)
    }

    @Test
    fun filterHidesIsolatesAndCanRestoreThem() {
        val bram = worldPerson("bram", "Bram")
        val isolated = worldPerson("loner", "Loner")
        val harpers = faction("harpers", "Harpers")
        val unused = faction("unused", "Unused")
        val web = factory.create(
            people = PeopleSnapshot(
                worldPeople = listOf(bram, isolated),
                campaignPeople = emptyList(),
            ),
            factions = listOf(harpers, unused),
            relationships = emptyList(),
            memberships = listOf(
                membership(
                    id = "mem-1",
                    person = PersonRef.World(bram.id),
                    factionId = harpers.id,
                )
            ),
        )

        val hidden = factory.filter(
            web = web,
            includeMemberships = true,
            enabledRelationshipTypes = RelationshipType.entries.toSet(),
            includeIsolates = false,
        )
        assertEquals(
            setOf(
                factory.personNodeId(PersonRef.World(bram.id)),
                factory.factionNodeId(harpers.id),
            ),
            hidden.nodes.map { it.id }.toSet(),
        )
        assertEquals(1, hidden.edges.size)

        val shown = factory.filter(
            web = web,
            includeMemberships = true,
            enabledRelationshipTypes = RelationshipType.entries.toSet(),
            includeIsolates = true,
        )
        assertEquals(4, shown.nodes.size)
        assertEquals(1, shown.edges.size)
    }

    @Test
    fun filterCanHideMembershipsAndRelationshipTypes() {
        val bram = worldPerson("bram", "Bram")
        val cora = worldPerson("cora", "Cora")
        val harpers = faction("harpers", "Harpers")
        val web = factory.create(
            people = PeopleSnapshot(worldPeople = listOf(bram, cora), campaignPeople = emptyList()),
            factions = listOf(harpers),
            relationships = listOf(
                relationship(
                    id = "rel-ally",
                    from = PersonRef.World(bram.id),
                    to = PersonRef.World(cora.id),
                    type = RelationshipType.Ally,
                ),
                relationship(
                    id = "rel-rival",
                    from = PersonRef.World(cora.id),
                    to = PersonRef.World(bram.id),
                    type = RelationshipType.Rival,
                ),
            ),
            memberships = listOf(
                membership(
                    id = "mem-1",
                    person = PersonRef.World(bram.id),
                    factionId = harpers.id,
                )
            ),
        )

        val noMemberships = factory.filter(
            web = web,
            includeMemberships = false,
            enabledRelationshipTypes = setOf(RelationshipType.Ally),
            includeIsolates = false,
        )
        assertEquals(1, noMemberships.edges.size)
        assertEquals("rel-ally", noMemberships.edges.single().id)
        assertEquals(2, noMemberships.nodes.size)
        assertTrue(noMemberships.nodes.none { it is RelationshipWeb.Node.Faction })
    }

    @Test
    fun worldAndCampaignPeopleKeepDistinctNodeIds() {
        val worldBram = worldPerson("same", "World Bram")
        val campaignBram = campaignPerson("same", "Campaign Bram")
        val web = factory.create(
            people = PeopleSnapshot(
                worldPeople = listOf(worldBram),
                campaignPeople = listOf(campaignBram),
            ),
            factions = emptyList(),
            relationships = listOf(
                relationship(
                    id = "rel-1",
                    from = PersonRef.World(worldBram.id),
                    to = PersonRef.Campaign(campaignBram.id),
                    type = RelationshipType.Mentor,
                )
            ),
            memberships = emptyList(),
        )

        assertEquals(2, web.nodes.size)
        assertEquals(1, web.edges.size)
        assertEquals(factory.personNodeId(PersonRef.World("same")), web.edges.single().fromId)
        assertEquals(factory.personNodeId(PersonRef.Campaign("same")), web.edges.single().toId)
    }

    private fun worldPerson(id: String, name: String): WorldPerson {
        return WorldPerson(
            id = id,
            worldId = "world-1",
            kind = PersonKind.Npc,
            name = name,
            description = "",
            sheet = FifthEditionSheet.empty(),
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun campaignPerson(id: String, name: String): CampaignPerson {
        return CampaignPerson(
            id = id,
            campaignId = "campaign-1",
            worldPersonId = null,
            kind = PersonKind.PlayerCharacter,
            name = name,
            description = "",
            sheet = FifthEditionSheet.empty(),
            overlayHitPoints = null,
            overlayNotes = "",
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun faction(id: String, name: String): Faction {
        return Faction(
            id = id,
            worldId = "world-1",
            name = name,
            description = "",
            goals = "",
            notes = "",
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun relationship(
        id: String,
        from: PersonRef,
        to: PersonRef,
        type: RelationshipType,
        factionId: String? = null,
    ): PersonRelationship {
        return PersonRelationship(
            id = id,
            from = from,
            to = to,
            type = type,
            description = "",
            factionId = factionId,
        )
    }

    private fun membership(
        id: String,
        person: PersonRef,
        factionId: String,
        role: String = "",
    ): FactionMembership {
        return FactionMembership(
            id = id,
            person = person,
            factionId = factionId,
            role = role,
            notes = "",
            createdAt = now,
        )
    }
}
