package eywa.projectcodex

import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.viewScores.data.ViewScoreData
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.After
import org.junit.Assert
import org.junit.Test

class ViewScoresUnitTest {
    private val distances = listOf(
            RoundDistance(1, 1, 1, 40),
            RoundDistance(1, 2, 1, 10)
    )
    private val arrowCounts = listOf(
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
}