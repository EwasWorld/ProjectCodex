package eywa.projectcodex

import android.content.res.Resources
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.After
import org.junit.Assert
import org.junit.Test

class ViewScoresUnitTest {
    private lateinit var resources: Resources
    private lateinit var arrows: List<ArrowValue>
    private var distances = listOf(
            RoundDistance(1, 1, 1, 40),
            RoundDistance(1, 2, 1, 10)
    )
    private var arrowCounts = listOf(
            RoundArrowCount(1, 1, 122.0, 9),
            RoundArrowCount(1, 2, 122.0, 9)
    )
    private val round = Round(1, "imperial", "imperial", false, false, listOf())

    @After
    fun teardown() {
        ViewScoreData.clearInstance()
    }

    @Test
    fun testGoldsTypesTens() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1), round)
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        Assert.assertEquals("2", entry.hitsScoreGolds.split("/").last())
    }

    @Test
    fun testGoldsTypesNines() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false))
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        Assert.assertEquals("3", entry.hitsScoreGolds.split("/").last())
    }

    @Test
    fun testScore() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false))
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        Assert.assertEquals(TestData.ARROWS.sumOf { it.score }.toString(), entry.hitsScoreGolds.split("/")[1])
    }

    @Test
    fun testHits() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false))
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        Assert.assertEquals((TestData.ARROWS.count() - 1).toString(), entry.hitsScoreGolds.split("/")[0])
    }

    @Test
    fun testHandicap() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1), round)
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        entry.updateArrowCounts(arrowCounts)
        entry.updateDistances(distances)

        Assert.assertEquals(
                Handicap.getHandicapForRound(
                        round,
                        arrowCounts,
                        distances,
                        TestData.ARROWS.sumOf { it.score },
                        false,
                        TestData.ARROWS.size
                ),
                entry.handicap
        )
    }

    @Test
    fun testRoundComplete() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1), round)
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        entry.updateArrowCounts(
                listOf(RoundArrowCount(1, 1, 122.0, TestData.ARROWS.size))
        )
        entry.updateDistances(distances)

        Assert.assertEquals(true, entry.isRoundComplete())
    }

    @Test
    fun testRoundIncomplete() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1), round)
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })
        entry.updateArrowCounts(arrowCounts)
        entry.updateDistances(distances)

        Assert.assertEquals(false, entry.isRoundComplete())
    }

    @Test
    fun testRoundNoRoundComplete() {
        val entry = ViewScoresEntry(
                ArcherRoundWithRoundInfoAndName(ArcherRound(1, TestData.generateDate(), 1, false))
        )
        entry.updateArrows(TestData.ARROWS.mapIndexed { i, arrow -> arrow.toArrowValue(1, i + 1) })

        Assert.assertEquals(false, entry.isRoundComplete())
    }

    @Test
    fun testUpdateArcherRoundData() {
        val ar1 = ArcherRound(1, TestData.generateDate(2021), 1, false)
        val ar1Edited = ArcherRound(1, TestData.generateDate(), 1, false, bowId = 5)
        val ar2 = ArcherRound(2, TestData.generateDate(2022), 1, false)
        val ar3 = ArcherRound(3, TestData.generateDate(2023), 1, false)

        val data = ViewScoreData.getViewScoreData()
        data.updateArcherRounds(listOf(ar1, ar2).map { ArcherRoundWithRoundInfoAndName(it) })

        Assert.assertEquals(ar1, data.getData().find { it.id == 1 }?.archerRound)
        Assert.assertEquals(ar2, data.getData().find { it.id == 2 }?.archerRound)

        // Sorted by date
        Assert.assertEquals(listOf(2, 1), data.getData().map { it.id })

        data.updateArcherRounds(listOf(ar1Edited, ar3).map { ArcherRoundWithRoundInfoAndName(it) })

        Assert.assertEquals(ar1Edited, data.getData().find { it.id == 1 }?.archerRound)
        Assert.assertEquals(null, data.getData().find { it.id == 2 })
        Assert.assertEquals(ar3, data.getData().find { it.id == 3 }?.archerRound)

        Assert.assertEquals(listOf(3, 1), data.getData().map { it.id })
    }

    @Test
    fun testUpdateArrowData() {
        val data = ViewScoreData.getViewScoreData()
        data.updateArcherRounds(
                listOf(
                        ArcherRoundWithRoundInfoAndName(
                                ArcherRound(
                                        1,
                                        TestData.generateDate(),
                                        1,
                                        false
                                )
                        )
                )
        )
        data.updateArrows(listOf(
                List(10) { i -> TestData.ARROWS[5].toArrowValue(1, i) },
                List(10) { i -> TestData.ARROWS[6].toArrowValue(2, i) }
        ).flatten())
        Assert.assertEquals("10/50/0", data.getData().first().hitsScoreGolds)
        data.updateArrows(listOf(
                List(10) { i -> TestData.ARROWS[7].toArrowValue(1, i) },
                List(10) { i -> TestData.ARROWS[8].toArrowValue(2, i) }
        ).flatten())
        data.updateArrows(listOf())
        Assert.assertEquals("-/-/-", data.getData().first().hitsScoreGolds)
    }

    @Test
    fun testUpdateArrowCountsData() {
        val data = ViewScoreData.getViewScoreData()
        data.updateArcherRounds(
                listOf(
                        ArcherRoundWithRoundInfoAndName(
                                ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1),
                                round
                        ),
                )
        )
        data.updateArrows(List(36) { i -> TestData.ARROWS[5].toArrowValue(1, i) })
        data.updateDistances(listOf(RoundDistance(1, 1, 1, 50)))

        data.updateArrowCounts(
                listOf(
                        RoundArrowCount(1, 1, 122.0, 36),
                        RoundArrowCount(2, 1, 122.0, 50),
                )
        )
        Assert.assertEquals(true, data.getData().first().isRoundComplete())

        data.updateArrowCounts(
                listOf(
                        RoundArrowCount(1, 1, 122.0, 50),
                        RoundArrowCount(2, 1, 122.0, 36),
                )
        )
        Assert.assertEquals(false, data.getData().first().isRoundComplete())

        // Set it to true so we can see it change to false when we remove data
        data.updateArrowCounts(listOf(RoundArrowCount(1, 1, 122.0, 36)))
        Assert.assertEquals(true, data.getData().first().isRoundComplete())

        data.updateArrowCounts(listOf())
        Assert.assertEquals(false, data.getData().first().isRoundComplete())
    }

    @Test
    fun testUpdateDistancesData() {
        val data = ViewScoreData.getViewScoreData()
        data.updateArcherRounds(
                listOf(
                        ArcherRoundWithRoundInfoAndName(
                                ArcherRound(1, TestData.generateDate(), 1, false, roundId = 1),
                                round
                        ),
                )
        )
        data.updateArrows(List(36) { i -> TestData.ARROWS[5].toArrowValue(1, i) })
        data.updateArrowCounts(listOf(RoundArrowCount(1, 1, 122.0, 36)))

        data.updateDistances(listOf(RoundDistance(1, 1, 1, 50)))
        Assert.assertTrue(data.getData().first().handicap!! > 0)

        data.updateDistances(listOf())
        Assert.assertEquals(null, data.getData().first().handicap)
    }

    @Test
    fun testScorePadDataToCsvNoRound() {
        setUpScorePadDetailsGlobals()
        val data = ScorePadData(arrows, 6, GoldsType.TENS, resources, null, null, null)
        val csv = data.getDetailsAsCsv(TestUtils.defaultColumnHeaderOrder, resources, true)

        val expected = """
            X-X-X-X-X-X,6,60,6,60
            10-10-10-10-10-10,6,60,6,120
            9-9-9-9-9-9,6,54,0,174
            8-8-8-8-8-8,6,48,0,222
            7-7-7-7-7-7,6,42,0,264
            6-6-6-6-6-6,6,36,0,300
            Grand Total,36,300,12,-
        """.trimIndent().trim()

        Assert.assertEquals("End,H,S,9,RT", csv.headerRow)
        Assert.assertEquals(expected, csv.details)
    }

    @Test
    fun testScorePadDataToCsvWithRound() {
        setUpScorePadDetailsGlobals()
        val data = ScorePadData(arrows, 6, GoldsType.TENS, resources, arrowCounts, distances, "m")
        val csv = data.getDetailsAsCsv(TestUtils.defaultColumnHeaderOrder, resources, true)

        val expected = """
            X-X-X-X-X-X,6,60,6,60
            10-10-10-10-10-10,6,60,6,120
            9-9-9-9-9-9,6,54,0,174
            Total at 10m,18,174,12,-
            8-8-8-8-8-8,6,48,0,222
            7-7-7-7-7-7,6,42,0,264
            6-6-6-6-6-6,6,36,0,300
            Total at 20m,18,126,0,-
            Grand Total,36,300,12,-
        """.trimIndent().trim()

        Assert.assertEquals("End,H,S,9,RT", csv.headerRow)
        Assert.assertEquals(expected, csv.details)
    }

    @Test
    fun testScorePadDataToCsvWithRoundNoDistanceTotals() {
        setUpScorePadDetailsGlobals()
        val data = ScorePadData(arrows, 6, GoldsType.TENS, resources, arrowCounts, distances, "m")
        val csv = data.getDetailsAsCsv(TestUtils.defaultColumnHeaderOrder, resources, false)

        val expected = """
            X-X-X-X-X-X,6,60,6,60
            10-10-10-10-10-10,6,60,6,120
            9-9-9-9-9-9,6,54,0,174
            8-8-8-8-8-8,6,48,0,222
            7-7-7-7-7-7,6,42,0,264
            6-6-6-6-6-6,6,36,0,300
            Grand Total,36,300,12,-
        """.trimIndent().trim()

        Assert.assertEquals("End,H,S,9,RT", csv.headerRow)
        Assert.assertEquals(expected, csv.details)
    }

    @Test
    fun testScorePadDataToStringNoRound() {
        setUpScorePadDetailsGlobals()
        val data = ScorePadData(arrows, 6, GoldsType.TENS, resources, null, null, null)
        val csv = data.getDetailsAsString(TestUtils.defaultColumnHeaderOrder, resources, true)

        val expected = """
           ||      X-X-X-X-X-X  6  60  6  60
            |10-10-10-10-10-10  6  60  6 120
            |      9-9-9-9-9-9  6  54  0 174
            |      8-8-8-8-8-8  6  48  0 222
            |      7-7-7-7-7-7  6  42  0 264
            |      6-6-6-6-6-6  6  36  0 300
            |      Grand Total 36 300 12   -
        """.trimMargin().trim().removePrefix("|")

        Assert.assertEquals(
                "              End  H   S  9  RT",
                csv.headerRow
        )
        Assert.assertEquals(expected, csv.details)
    }

    @Test
    fun testScorePadDataToStringWithRound() {
        setUpScorePadDetailsGlobals()
        val data = ScorePadData(arrows, 6, GoldsType.TENS, resources, arrowCounts, distances, "m")
        val csv = data.getDetailsAsString(TestUtils.defaultColumnHeaderOrder, resources, true)

        val expected = """
           ||      X-X-X-X-X-X  6  60  6  60
            |10-10-10-10-10-10  6  60  6 120
            |      9-9-9-9-9-9  6  54  0 174
            |     Total at 10m 18 174 12   -
            |      8-8-8-8-8-8  6  48  0 222
            |      7-7-7-7-7-7  6  42  0 264
            |      6-6-6-6-6-6  6  36  0 300
            |     Total at 20m 18 126  0   -
            |      Grand Total 36 300 12   -
        """.trimMargin().trim().removePrefix("|")

        Assert.assertEquals(
                "              End  H   S  9  RT",
                csv.headerRow
        )
        Assert.assertEquals(expected, csv.details)
    }

    @Test
    fun testScorePadDataToStringWithRoundNoDistanceTotals() {
        setUpScorePadDetailsGlobals()
        val data = ScorePadData(arrows, 6, GoldsType.TENS, resources, arrowCounts, distances, "m")
        val csv = data.getDetailsAsString(TestUtils.defaultColumnHeaderOrder, resources, false)

        val expected = """
           ||      X-X-X-X-X-X  6  60  6  60
            |10-10-10-10-10-10  6  60  6 120
            |      9-9-9-9-9-9  6  54  0 174
            |      8-8-8-8-8-8  6  48  0 222
            |      7-7-7-7-7-7  6  42  0 264
            |      6-6-6-6-6-6  6  36  0 300
            |      Grand Total 36 300 12   -
        """.trimMargin().trim().removePrefix("|")

        Assert.assertEquals(
                "              End  H   S  9  RT",
                csv.headerRow
        )
        Assert.assertEquals(expected, csv.details)
    }

    private fun setUpScorePadDetailsGlobals() {
        resources = TestUtils.createResourceMock(
                mapOf(
                        Pair(R.string.end_to_string_arrow_placeholder, TestData.ARROW_PLACEHOLDER),
                        Pair(R.string.end_to_string_arrow_deliminator, TestData.ARROW_DELIMINATOR),
                        Pair(R.string.score_pad__grand_total, "Grand Total"),
                        Pair(R.string.score_pad__running_total_placeholder, "-"),
                        Pair(R.string.score_pad__distance_total, "Total at {distance}{unit}"),
                        Pair(R.string.score_pad__surplus_total, "Surplus Total"),
                        Pair(R.string.score_pad__end_string_header, "End"),
                        Pair(R.string.table_hits_header, "H"),
                        Pair(R.string.table_score_header, "S"),
                        Pair(R.string.table_golds_tens_header, "9"),
                        Pair(R.string.score_pad__running_total_header, "RT"),
                )
        )
        arrows = List(6) { i -> List(6) { TestData.ARROWS[TestData.ARROWS.size - i - 1] } }.flatten()
                .mapIndexed { i, arrow -> arrow.toArrowValue(1, i) }
        arrowCounts = List(2) { i -> RoundArrowCount(1, i, 1.0, 18) }
        distances = List(2) { i -> RoundDistance(1, i, 1, (i + 1) * 10) }
    }
}