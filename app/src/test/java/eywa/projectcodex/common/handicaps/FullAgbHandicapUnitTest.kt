package eywa.projectcodex.common.handicaps

import eywa.projectcodex.common.utils.ListUtils.transpose
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.Handicap.HandicapPair
import eywa.projectcodex.model.roundHandicap
import eywa.projectcodex.testUtils.RawResourcesHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

class FullAgbHandicapUnitTest {
    private suspend fun getHandicapData(): List<HandicapData> {
        val allRounds = RawResourcesHelper.getDefaultRounds().associateBy { it.round.defaultRoundId!! }
        assertEquals(29, allRounds.size)

        val handicapData = RawResourcesHelper.rawAgbHandicapData
                // String -> 2d array
                .split("\n").map { it.split(",") }
                .transpose()
                // Ignore headings
                .drop(1)
                .map { HandicapData.fromCsvRow(it, allRounds) }

        assertEquals(115, handicapData.size)
        return handicapData
    }

    @Test
    fun test2023HandicapToScore() = runTest {
        val handicapData = getHandicapData()

        val incorrect = mutableListOf<HandicapOutcome>()
        val freq = mutableMapOf<Int, Int>()

        for (data in handicapData) {
            val pairs = data.getHandicapsToTest()

            for (pair in pairs) {
                val (handicap, score) = pair
                val outcome = Handicap.getScoreForRound(
                        round = data.roundInfo,
                        subType = data.roundInfo.roundSubTypes?.firstOrNull()?.subTypeId,
                        handicap = handicap,
                        innerTenArcher = data.isCompound,
                        arrows = null,
                        use2023Handicaps = true,
                        faces = listOf(data.faceType),
                )!!

                val diff = (outcome - score)
                if (abs(diff) > 0) {
                    incorrect.add(HandicapOutcome(pair, outcome.toDouble(), data.roundName))
                    freq[diff] = freq.getOrDefault(diff, 0) + 1
                }
            }

            // Check that a reasonable number of tests have run
            Assert.assertTrue(pairs.size > 80)
        }

        assertEquals(emptyList<String>(), incorrect.map { it.toHandicapString() })
    }

    @Test
    fun testScoreTo2023Handicap() = runTest {
        val handicapData = getHandicapData()

        val incorrect = mutableListOf<HandicapOutcome>()
        val freq = mutableMapOf<Int, Int>()

        for (data in handicapData) {
            val pairs = data.getScoresToTest()

            for (pair in pairs) {
                val (handicap, score) = pair
                val outcome = Handicap.getHandicapForRound(
                        round = data.roundInfo,
                        subType = data.roundInfo.roundSubTypes?.firstOrNull()?.subTypeId,
                        score = score,
                        innerTenArcher = data.isCompound,
                        arrows = null,
                        use2023Handicaps = true,
                        faces = listOf(data.faceType),
                )!!

                val diff = (outcome.roundHandicap() - handicap.roundHandicap())
                if (abs(diff) > 0 && !(handicap.roundHandicap() == 150 && outcome > 150)) {
                    incorrect.add(HandicapOutcome(pair, outcome, data.roundName))
                    freq[diff] = freq.getOrDefault(diff, 0) + 1
                }
            }

            // Check that a reasonable number of tests have run
            Assert.assertTrue("pairs too small: ${pairs.size}", pairs.size > 130)
        }

        assertEquals(emptyList<String>(), incorrect.map { it.toScoreString() })
    }

    data class HandicapData(
            val roundInfo: FullRoundInfo,
            val isCompound: Boolean,
            val faceType: RoundFace,
            val roundName: String,
            val scores: List<Int>,
    ) {
        fun getHandicapsToTest(): List<HandicapPair> = getScoresToTest(false)

        fun getScoresToTest(testScores: Boolean = true): List<HandicapPair> {
            val tests = mutableListOf<HandicapPair>()

            for ((handicap, score) in scores.withIndex()) {
                val prevScore = scores.getOrNull(handicap - 1)
                val nextScore = scores.getOrNull(handicap + 1)

                if (!testScores && prevScore == score && nextScore == score) {
                    continue
                }
                if (testScores && nextScore == score) {
                    continue
                }
                tests.add(HandicapPair(handicap = handicap.toDouble(), score = score))

                if (testScores && nextScore != null && (score - 1) > nextScore) {
                    tests.add(HandicapPair(handicap = handicap + 1.0, score = score - 1))
                }
            }

            return tests
        }

        companion object {
            fun fromCsvRow(data: List<String>, dbData: Map<Int, FullRoundInfo>): HandicapData {

                val isCompound = data[4].takeIfNotNull() == "1"
                val faceType = data[5].takeIfNotNull()?.let { RoundFace.valueOf(it.uppercase()) } ?: RoundFace.FULL
                val roundId = data[7].takeIfNotNull()?.toInt()
                val roundName = data[8].takeIfNotNull()!!
                val scores = data.drop(9).map { it.toInt() }

                check(scores.size == 151) { "Scores size should be 151: ${scores.size}" }

                return HandicapData(
                        roundInfo = (
                                if (roundId != null) getFullRoundInfoFromRoundId(data, dbData)
                                else getFullRoundInfoFromSingleDistance(data)
                                ),
                        isCompound = isCompound,
                        faceType = faceType,
                        roundName = roundName,
                        scores = scores,
                )
            }

            private fun String.takeIfNotNull() = takeIf { it != "null" }

            private fun getFullRoundInfoFromRoundId(
                    data: List<String>,
                    dbData: Map<Int, FullRoundInfo>
            ): FullRoundInfo {
                val subtypeId = data[6].takeIfNotNull()?.toInt() ?: 1
                val roundId = data[7].takeIfNotNull()?.toInt()

                val info = dbData[roundId]!!
                return info.copy(
                        roundSubTypes = info.roundSubTypes?.filter { it.subTypeId == subtypeId },
                        roundDistances = info.roundDistances?.filter { it.subTypeId == subtypeId },
                )
            }

            private fun getFullRoundInfoFromSingleDistance(data: List<String>): FullRoundInfo {
                val isMetric = data[0].takeIfNotNull() == "1"
                val arrowCount = data[1].takeIfNotNull()?.toInt()!!
                val faceSizeInCm = data[2].takeIfNotNull()?.toInt()!!
                val distance = data[3].takeIfNotNull()?.toInt()!!
                val roundName = data[8].takeIfNotNull()!!

                return FullRoundInfo(
                        round = Round(
                                roundId = 1,
                                name = roundName,
                                displayName = roundName,
                                isOutdoor = true,
                                isMetric = isMetric,
                                fiveArrowEnd = roundName.lowercase().contains("worcester"),
                                legacyName = null,
                                defaultRoundId = null,
                        ),
                        roundSubTypes = emptyList(),
                        roundArrowCounts = listOf(
                                RoundArrowCount(
                                        roundId = 1,
                                        distanceNumber = 1,
                                        faceSizeInCm = faceSizeInCm.toDouble(),
                                        arrowCount = arrowCount,
                                )
                        ),
                        roundDistances = listOf(
                                RoundDistance(
                                        roundId = 1,
                                        distanceNumber = 1,
                                        subTypeId = 1,
                                        distance = distance,
                                )
                        ),
                )
            }
        }
    }

    data class HandicapOutcome(
            val expected: HandicapPair,
            val actual: Double,
            val roundName: String,
    ) {
        fun toHandicapString() = "$roundName - HC: ${expected.handicap} - expected: ${expected.score}, actual: $actual"
        fun toScoreString() =
                "$roundName - score: ${expected.score} - expected: ${expected.handicap}, actual: ${actual.roundHandicap()} ($actual)"
    }
}

