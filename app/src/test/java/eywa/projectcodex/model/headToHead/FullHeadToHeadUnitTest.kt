package eywa.projectcodex.model.headToHead

import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadPreviewHelperDsl
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import org.junit.Assert.assertEquals
import org.junit.Test

class FullHeadToHeadUnitTest {
    @Test
    fun testConversion() {
        val data = HeadToHeadPreviewHelperDsl(2).apply {
            addHeat {
                addSet {
                    addRows(
                            result = HeadToHeadResult.WIN,
                            typesToIsTotal = mapOf(
                                    HeadToHeadArcherType.SELF to false,
                                    HeadToHeadArcherType.OPPONENT to true,
                            ),
                            winnerScore = 25,
                            loserScore = 20,
                            dbIds = listOf(listOf(1, 2, 3), listOf(4))
                    )
                }
                addSet {
                    addRows(
                            result = HeadToHeadResult.LOSS,
                            typesToIsTotal = mapOf(HeadToHeadArcherType.RESULT to true),
                            winnerScore = 30,
                            loserScore = 29,
                            dbIds = listOf(listOf(5))
                    )
                }
                addSet { addRows(result = HeadToHeadResult.TIE) }
                addSet { addRows(result = HeadToHeadResult.WIN) }
                addSet { addRows(result = HeadToHeadResult.LOSS) }
                addSet { addRows(result = HeadToHeadResult.LOSS, winnerScore = 10, loserScore = 1) }
            }
            addHeat {
                addSet { addRows(result = HeadToHeadResult.WIN) }
                addSet { addRows(result = HeadToHeadResult.LOSS) }
                addSet { addRows(result = HeadToHeadResult.TIE) }
            }
            addHeat {
                addSet { addRows() }
                addSet { addRows() }
                addSet { addRows() }
            }
        }

        val original = data.asFull()
        val converted = FullHeadToHead(
                headToHead = original.headToHead,
                heats = original.heats.map { it.heat },
                details = original.heats.flatMap { heat ->
                    heat.sets.flatMap {
                        it.asDatabaseDetails(shootId = original.headToHead.shootId, heat = heat.heat.heat)
                    }
                },
                isEditable = false,
        )

        assertEquals(original, converted)
    }
}
