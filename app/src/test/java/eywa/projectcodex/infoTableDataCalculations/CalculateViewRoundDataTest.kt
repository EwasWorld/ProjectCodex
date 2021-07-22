package eywa.projectcodex.infoTableDataCalculations

import eywa.projectcodex.TestData
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class CalculateViewRoundDataTest {
    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK)
    private val removedColumnIndexes = listOf(0)
    private val maxArrowCount = 2000
    private val defaultArrowSize = 36
    private val defaultArrows = listOf(TestData.ARROWS[11], TestData.ARROWS[10], TestData.ARROWS[9], TestData.ARROWS[0])
    private val allRounds = listOf(
            Round(1, "round1", "Round1", true, true, listOf()),
            Round(2, "round2", "Round2", true, false, listOf()),
            Round(3, "round3", "Round3", false, true, listOf())
    )
    private val allDistances = listOf(
            RoundDistance(1, 1, 1, 40),
            RoundDistance(1, 1, 2, 10),
            RoundDistance(2, 1, 1, 50),
            RoundDistance(2, 1, 2, 20),
            RoundDistance(3, 1, 1, 60),
            RoundDistance(3, 1, 2, 30)
    )
    val allArrowCounts = listOf(
            RoundArrowCount(1, 1, 122.0, maxArrowCount),
            RoundArrowCount(2, 1, 122.0, maxArrowCount),
            RoundArrowCount(3, 1, 122.0, maxArrowCount)
    )
    private val defaultArcherRounds = listOf(
            ArcherRound(1, TestData.generateDate(), 1, true, roundId = 1),
            ArcherRound(2, TestData.generateDate(), 1, true, roundId = 2),
            ArcherRound(3, TestData.generateDate(), 1, true, roundId = 3),
            ArcherRound(4, TestData.generateDate(), 1, true, roundId = 1, roundSubTypeId = 1),
            ArcherRound(5, TestData.generateDate(), 1, true, roundId = 2, roundSubTypeId = 2),
            ArcherRound(6, TestData.generateDate(), 1, true)
    )

    @Test
    fun testGoldsTypes() {
        var testIndex = 0
        for (testGoldsType in GoldsType.values()) {
            println(testIndex++)
            checkViewRoundsData(listOf(), testGoldsType)
        }
    }

    @Test
    fun testDifferentRoundSizes() {
        var testIndex = 0
        for (testArrowsSizes in listOf(listOf(36, 0, 6, 36, 45, 144, 60, 60))) {
            println(testIndex++)
            checkViewRoundsData(testArrowsSizes, GoldsType.TENS)
        }
    }

    @Test
    fun testNoData() {
        Assert.assertTrue(
                calculateViewRoundsTableData(listOf(), TestData.generateArrowValues(3, 1), GoldsType.TENS)
                        .isNullOrEmpty()
        )
    }


    /**
     * @param arrowsSizes size of list is how many rounds to test, value is number of arrows for that round
     */
    private fun checkViewRoundsData(arrowsSizes: List<Int>, goldsType: GoldsType) {
        require(arrowsSizes.all { it < maxArrowCount }) { "arrow count too big, increase the max" }

        val arrowSizesToUse = arrowsSizes.plus(
                List((defaultArcherRounds.size - arrowsSizes.size).coerceAtLeast(0)) { defaultArrowSize }
        )

        val archerRounds = defaultArcherRounds
                .plus(List((arrowSizesToUse.size - defaultArcherRounds.size).coerceAtLeast(0)) { i ->
                    ArcherRound(defaultArcherRounds.last().archerRoundId + 1 + i, TestData.generateDate(), 1, true)
                })
                .map { ar ->
                    ArcherRoundWithRoundInfoAndName(
                            ar,
                            allRounds.find { it.roundId == ar.roundId },
                            ar.roundSubTypeId?.toString() ?: ""
                    )
                }
        val sortedArcherRounds = archerRounds.sortedByDescending { it.archerRound.dateShot }
        val allArrows = mutableListOf<List<ArrowValue>>()
        val arrowList =
                defaultArrows.plus(List((maxArrowCount - defaultArrows.size).coerceAtLeast(0)) { TestData.ARROWS[5] })
        for (i in arrowSizesToUse.indices) {
            allArrows.add(arrowList.take(arrowSizesToUse[i]).mapIndexed { j, arrow ->
                arrow.toArrowValue(sortedArcherRounds[i].archerRound.archerRoundId, j)
            })
        }

        require(sortedArcherRounds.size == arrowSizesToUse.size)
        require(sortedArcherRounds.size == allArrows.size)

        val viewRoundsData =
                calculateViewRoundsTableData(
                        archerRounds,
                        allArrows.flatten(),
                        goldsType,
                        allArrowCounts,
                        allDistances
                )
        Assert.assertEquals(archerRounds.size, viewRoundsData.size)

        for (i in sortedArcherRounds.indices) {
            val archerRound = sortedArcherRounds[i].archerRound

            val arrows = allArrows[i]
            val data = viewRoundsData[i]

            val expected = mutableListOf<Any>()
            expected.add(archerRound.archerRoundId)
            expected.add(dateFormat.format(archerRound.dateShot))
            expected.add(sortedArcherRounds[i].roundSubTypeName ?: sortedArcherRounds[i].round?.displayName ?: "")
            expected.add(arrows.count { it.score != 0 })
            expected.add(arrows.sumOf { it.score })
            expected.add(
                    when {
                        archerRound.roundId == 2 -> 3
                        archerRound.roundId != null -> 2
                        goldsType == GoldsType.NINES -> 3
                        goldsType == GoldsType.TENS -> 2
                        goldsType == GoldsType.XS -> 1
                        else -> throw IllegalStateException("Cannot calculate expected golds")
                    }.coerceAtMost(arrows.size)
            )
            // + 1 for handicap col
            Assert.assertEquals(expected.size + 1, data.size)

            for (j in expected.indices.filterIndexed { k, _ -> !removedColumnIndexes.contains(k) }) {
                Assert.assertEquals("cell$i$j", data[j].id)
                if (j == expected.size) {
                    if (archerRound.roundId == null) {
                        Assert.assertEquals("-", data[j].content)
                    }
                    else {
                        Assert.assertTrue(data[j].content as Int > 0)
                    }
                    continue
                }
                Assert.assertEquals("set: $i, cell: ${data[j].id}", expected[j], data[j].content)
            }
        }
    }
}