package eywa.projectcodex.common

import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge.*
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow.*
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.testUtils.RawResourcesHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClassificationTablesUseCaseUnitTest {
    private val classificationTables = RawResourcesHelper.classificationTables

    @Test
    fun testNoDuplicateRoundRefs() {
        assertEquals(
                ClassificationRound.values().size,
                ClassificationRound.values().distinctBy { it.rounds }.size,
        )
    }

    @Test
    fun testMensYork() = runTest {
        val round = RawResourcesHelper.getDefaultRounds().find { it.round.displayName == "York / Hereford" }!!

        val entries = classificationTables.get(
                isGent = true,
                age = SENIOR,
                bow = RECURVE,
                fullRoundInfo = round,
                roundSubTypeId = 1,
        )!!

        assertEquals(
                ClassificationTableEntry(
                        classification = Classification.ARCHER_1ST_CLASS,
                        isGent = true,
                        bowStyle = RECURVE,
                        round = ClassificationRound.DbRoundRef(1, 1),
                        age = SENIOR,
                        score = 534,
                        handicap = 58,
                ),
                entries.find { it.classification == Classification.ARCHER_1ST_CLASS },
        )
        assertEquals(
                ClassificationTableEntry(
                        classification = Classification.ELITE_MASTER_BOWMAN,
                        isGent = true,
                        bowStyle = RECURVE,
                        round = ClassificationRound.DbRoundRef(1, 1),
                        age = SENIOR,
                        score = 1205,
                        handicap = 16,
                ),
                entries.find { it.classification == Classification.ELITE_MASTER_BOWMAN },
        )
    }

    @Test
    fun testWomensHereford() = runTest {
        val round = RawResourcesHelper.getDefaultRounds().find { it.round.displayName == "York / Hereford" }!!

        val entries = classificationTables.get(
                isGent = false,
                age = SENIOR,
                bow = RECURVE,
                fullRoundInfo = round,
                roundSubTypeId = 2,
        )!!

        assertEquals(
                ClassificationTableEntry(
                        classification = Classification.ARCHER_1ST_CLASS,
                        isGent = false,
                        bowStyle = RECURVE,
                        round = ClassificationRound.DbRoundRef(1, 2),
                        age = SENIOR,
                        score = 614,
                        handicap = 63,
                ),
                entries.find { it.classification == Classification.ARCHER_1ST_CLASS },
        )
        assertEquals(
                ClassificationTableEntry(
                        classification = Classification.ELITE_MASTER_BOWMAN,
                        isGent = false,
                        bowStyle = RECURVE,
                        round = ClassificationRound.DbRoundRef(1, 2),
                        age = SENIOR,
                        score = 1232,
                        handicap = 21,
                ),
                entries.find { it.classification == Classification.ELITE_MASTER_BOWMAN },
        )
    }

    @Test
    fun testMensU15CompoundGents1440() = runTest {
        val round = RawResourcesHelper.getDefaultRounds().find { it.round.displayName == "WA 1440 / FITA" }!!

        val entries = classificationTables.get(
                isGent = true,
                age = U15,
                bow = COMPOUND,
                fullRoundInfo = round,
                roundSubTypeId = 1,
        )!!

        assertEquals(
                ClassificationTableEntry(
                        classification = Classification.ARCHER_1ST_CLASS,
                        isGent = true,
                        bowStyle = COMPOUND,
                        round = ClassificationRound.DbRoundRef(8, 1),
                        age = U15,
                        score = 609,
                        handicap = 63,
                ),
                entries.find { it.classification == Classification.ARCHER_1ST_CLASS },
        )
        assertEquals(
                ClassificationTableEntry(
                        classification = Classification.ELITE_MASTER_BOWMAN,
                        isGent = true,
                        bowStyle = COMPOUND,
                        round = ClassificationRound.DbRoundRef(8, 1),
                        age = U15,
                        score = 1229,
                        handicap = 27,
                ),
                entries.find { it.classification == Classification.ELITE_MASTER_BOWMAN },
        )
    }

    /**
     * When no round is selected, ClassificationTablesScreen will show a rough handicap for each classification given
     * the selected categories. The handicap is chosen from either the Gents 1440 or the Metric V. These two were
     * selected as the Gents 1440 guarantees all classifications will be covered and the Metric V makes handicaps
     * at the lower end more accurate (as the Gents 1440 has a lot of overlapping handicaps that round to the worst,
     * giving an inaccurate guess that is worse than it should be).
     *
     * This test checks that the Gents 1440 and the Metric V are roughly correct for handicaps. There are a few
     * [expectedFails] that are known to be slightly off and have been accepted as such
     */
    @Test
    fun testHandicapsAreSameAs1440() = runTest {
        val rounds = RawResourcesHelper.getDefaultRounds().associateBy { it.round.defaultRoundId }
        val gents1440 = rounds[8]!!

        ClassificationBow.values().forEach { bow ->
            ClassificationAge.values().forEach { age ->
                listOf(true, false).forEach { isGent ->
                    rounds.forEach { (_, round) ->
                        (round.roundSubTypes?.map { it.subTypeId } ?: listOf(1)).forEach loop@{ subTypeId ->
                            if (FailingItem(bow, round.round.defaultRoundId!!, subTypeId, age) in expectedFails) {
                                return@loop
                            }

                            val actualEntries =
                                    classificationTables.get(
                                            isGent = isGent,
                                            age = age,
                                            bow = bow,
                                            fullRoundInfo = round,
                                            roundSubTypeId = subTypeId,
                                    )!!.sortedByDescending { it.handicap }
                            if (actualEntries.isEmpty()) return@loop

                            val expectedEntries =
                                    classificationTables.getRoughHandicaps(
                                            isGent = isGent,
                                            age = age,
                                            bow = bow,
                                            wa1440RoundInfo = gents1440,
                                    )!!.sortedByDescending { it.handicap }

                            val actualHandicaps = actualEntries.map { it.handicap }

                            actualEntries.forEachIndexed { index, it ->
                                val actualHandicap = actualHandicaps[index]
                                val expectedHandicap = expectedEntries
                                        .find { entry -> entry.classification == it.classification }
                                        ?.handicap

                                if (expectedHandicap == actualHandicap) {
                                    return@forEachIndexed
                                }

                                val expectedScore = expectedHandicap?.let {
                                    Handicap.getScoreForRound(
                                            round = round,
                                            subType = subTypeId,
                                            handicap = it.toDouble(),
                                            innerTenArcher = bow == COMPOUND,
                                            use2023Handicaps = true,
                                    )
                                }
                                if (expectedScore == it.score) {
                                    return@forEachIndexed
                                }
                                fail()
                            }
                        }
                    }
                }
            }
        }
    }

    private data class FailingItem(
            val bowStyle: ClassificationBow,
            val defaultRoundId: Int,
            val subTypeId: Int,
            val age: ClassificationAge,
            val isGent: Boolean = true,
            val offBy: Int = 0,
    )

    private val expectedFails = listOf(
            // Long National
            FailingItem(bowStyle = RECURVE, defaultRoundId = 6, subTypeId = 2, age = U14),
            // New Warwick
            FailingItem(bowStyle = RECURVE, defaultRoundId = 7, subTypeId = 1, age = U14),
            FailingItem(bowStyle = RECURVE, defaultRoundId = 7, subTypeId = 1, age = U12),
            FailingItem(bowStyle = BAREBOW, defaultRoundId = 7, subTypeId = 1, age = U12),
            // New National
            FailingItem(bowStyle = BAREBOW, defaultRoundId = 6, subTypeId = 1, age = U12),
            // Long Warwick
            FailingItem(bowStyle = BAREBOW, defaultRoundId = 7, subTypeId = 2, age = U14),
            FailingItem(bowStyle = BAREBOW, defaultRoundId = 7, subTypeId = 2, age = U12),
    )
}
