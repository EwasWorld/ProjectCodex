package eywa.projectcodex.common

import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge.*
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow.*
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound
import eywa.projectcodex.testUtils.RawResourcesHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClassificationTablesUseCaseUnitTest {
    private val classificationTables = RawResourcesHelper.classificationTables

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
                        rounds = listOf(ClassificationRound.DbRoundRef(1, 1)),
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
                        rounds = listOf(ClassificationRound.DbRoundRef(1, 1)),
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
                        rounds = listOf(ClassificationRound.DbRoundRef(1, 2)),
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
                        rounds = listOf(ClassificationRound.DbRoundRef(1, 2)),
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
                        rounds = listOf(ClassificationRound.DbRoundRef(8, 1)),
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
                        rounds = listOf(ClassificationRound.DbRoundRef(8, 1)),
                        age = U15,
                        score = 1229,
                        handicap = 27,
                ),
                entries.find { it.classification == Classification.ELITE_MASTER_BOWMAN },
        )
    }

    @Test
    fun testHandicapsAreSameAsYork() = runTest {
        val rounds = RawResourcesHelper.getDefaultRounds().associateBy { it.round.defaultRoundId }
        val york = rounds[1]!!

        val fails = mutableListOf<FailingItem>()

        ClassificationBow.values().forEach { bow ->
            ClassificationAge.values().forEach { age ->
                listOf(true, false).forEach { isGent ->
                    rounds.forEach { (_, round) ->
                        (round.roundSubTypes?.map { it.subTypeId } ?: listOf(1)).forEach loop@{ subTypeId ->
                            // TODO Do you want to do anything about these fails?
                            if (bow == LONGBOW) return@loop
                            if (
                                FailingItem(
                                        isGent = true,
                                        bowStyle = bow,
                                        defaultRoundId = round.round.defaultRoundId!!,
                                        subTypeId = subTypeId,
                                        age = age,
                                ) in expectedFails
                            ) return@loop

                            val actualEntries =
                                    classificationTables.get(
                                            isGent = isGent,
                                            age = age,
                                            bow = bow,
                                            fullRoundInfo = round,
                                            roundSubTypeId = subTypeId,
                                    )!!

                            val actualHandicaps = actualEntries.map { it.handicap }.sortedByDescending { it }
                            val yorkHandicaps =
                                    classificationTables.get(
                                            isGent = isGent,
                                            age = age,
                                            bow = bow,
                                            fullRoundInfo = york,
                                            roundSubTypeId = 1,
                                    )!!.map { it.handicap }.sortedByDescending { it }.take(actualEntries.size)
                            val isEqual = actualHandicaps == yorkHandicaps
                            if (!isEqual) {
                                fails.add(
                                        FailingItem(
                                                isGent = isGent,
                                                bowStyle = bow,
                                                defaultRoundId = round.round.defaultRoundId!!,
                                                subTypeId = subTypeId,
                                                age = age,
                                                offBy = actualHandicaps.sumOf { it ?: 0 } -
                                                        yorkHandicaps.sumOf { it ?: 0 }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        assertEquals(emptyList<FailingItem>(), fails)
    }

    private data class FailingItem(
            val isGent: Boolean,
            val bowStyle: ClassificationBow,
            val defaultRoundId: Int,
            val subTypeId: Int,
            val age: ClassificationAge,
            val offBy: Int = 0,
    )

    private val expectedFails = listOf(
            // Long National
            FailingItem(isGent = true, bowStyle = RECURVE, defaultRoundId = 6, subTypeId = 2, age = U14),
            // New Warwick
            FailingItem(isGent = true, bowStyle = RECURVE, defaultRoundId = 7, subTypeId = 1, age = U14),
            FailingItem(isGent = true, bowStyle = RECURVE, defaultRoundId = 7, subTypeId = 1, age = U12),
            FailingItem(isGent = true, bowStyle = BAREBOW, defaultRoundId = 7, subTypeId = 1, age = U12),
            // New National
            FailingItem(isGent = true, bowStyle = BAREBOW, defaultRoundId = 6, subTypeId = 1, age = U12),
            // Long Warwick
            FailingItem(isGent = true, bowStyle = BAREBOW, defaultRoundId = 7, subTypeId = 2, age = U14),
            FailingItem(isGent = true, bowStyle = BAREBOW, defaultRoundId = 7, subTypeId = 2, age = U12),
    )
}
