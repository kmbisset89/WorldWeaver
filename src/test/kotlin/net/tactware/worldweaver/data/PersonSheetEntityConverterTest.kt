package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.domain.Pathfinder2ESheet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class PersonSheetEntityConverterTest {
    private val converter = PersonSheetEntityConverter(
        fifthEditionConverter = FifthEditionSheetConverter(),
        pathfinderConverter = Pathfinder2ESheetConverter(),
    )

    @Test
    fun fifthEditionRowsStayFifthEdition() {
        val sheet = FifthEditionSheet.empty().copy(race = "Elf (High)", walkSpeed = 35)
        val encoded = converter.encode(sheet)

        assertEquals(GameSystem.FifthEdition.name, encoded.sheetSystem)
        assertEquals("", encoded.pf2ePayload)

        val decoded = converter.decode(
            sheetSystem = encoded.sheetSystem,
            encodedFifthEdition = encoded.fifthEdition,
            pf2ePayload = encoded.pf2ePayload,
        )

        assertIs<FifthEditionSheet>(decoded)
        assertEquals("Elf (High)", decoded.race)
        assertEquals(35, decoded.walkSpeed)
    }

    @Test
    fun missingSheetSystemDecodesAsFifthEdition() {
        val fifth = FifthEditionSheetConverter().encode(FifthEditionSheet.empty().copy(race = "Human"))

        val decoded = converter.decode(
            sheetSystem = "",
            encodedFifthEdition = fifth,
            pf2ePayload = "",
        )

        val sheet = assertIs<FifthEditionSheet>(decoded)
        assertEquals("Human", sheet.race)
    }

    @Test
    fun pathfinderPayloadRoundTrips() {
        val sheet = Pathfinder2ESheet.empty().copy(ancestry = "Dwarf", className = "Fighter")
        val encoded = converter.encode(sheet)

        assertEquals(GameSystem.Pathfinder2E.name, encoded.sheetSystem)
        assertTrue(encoded.pf2ePayload.isNotBlank())

        val decoded = converter.decode(
            sheetSystem = encoded.sheetSystem,
            encodedFifthEdition = encoded.fifthEdition,
            pf2ePayload = encoded.pf2ePayload,
        )

        val pf2e = assertIs<Pathfinder2ESheet>(decoded)
        assertEquals("Dwarf", pf2e.ancestry)
        assertEquals("Fighter", pf2e.className)
    }
}
