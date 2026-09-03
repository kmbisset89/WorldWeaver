package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.domain.Pathfinder2ESheet
import net.tactware.worldweaver.domain.PersonSheet

internal class PersonSheetEntityConverter(
    private val fifthEditionConverter: FifthEditionSheetConverter,
    private val pathfinderConverter: Pathfinder2ESheetConverter,
) {
    fun decode(
        sheetSystem: String,
        encodedFifthEdition: FifthEditionSheetConverter.EncodedSheet,
        pf2ePayload: String,
    ): PersonSheet {
        return when (GameSystem.fromStorage(sheetSystem)) {
            GameSystem.Pathfinder2E -> pathfinderConverter.decode(pf2ePayload)
            GameSystem.FifthEdition -> fifthEditionConverter.decode(encodedFifthEdition)
        }
    }

    fun encode(sheet: PersonSheet): EncodedPersonSheet {
        return when (sheet) {
            is FifthEditionSheet -> EncodedPersonSheet(
                sheetSystem = GameSystem.FifthEdition.name,
                fifthEdition = fifthEditionConverter.encode(sheet),
                pf2ePayload = "",
            )
            is Pathfinder2ESheet -> EncodedPersonSheet(
                sheetSystem = GameSystem.Pathfinder2E.name,
                fifthEdition = fifthEditionConverter.encode(FifthEditionSheet.empty()),
                pf2ePayload = pathfinderConverter.encode(sheet),
            )
        }
    }

    data class EncodedPersonSheet(
        val sheetSystem: String,
        val fifthEdition: FifthEditionSheetConverter.EncodedSheet,
        val pf2ePayload: String,
    )
}
