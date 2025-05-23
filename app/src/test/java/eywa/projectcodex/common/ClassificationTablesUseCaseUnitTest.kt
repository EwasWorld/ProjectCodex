package eywa.projectcodex.common

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge.*
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow.*
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationRound
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.testUtils.RawResourcesHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ClassificationTablesUseCaseUnitTest {
    @Test
    fun testRows() {
        val classificationData = RawResourcesHelper.rawClassificationData.split("\n")

        val failed = mutableListOf<String>()
        classificationData.forEach {
            try {
                ClassificationTableEntry.fromString(it)
            }
            catch (e: NullPointerException) {
                failed.add(it)
            }
        }

        println(failed.map { it.split(",")[4] }.distinct())
        assertEquals(mutableListOf<String>(), failed)
    }

    @Test
    fun testNoDuplicateRoundRefs() {
        fun ClassificationRound.DbRoundRef.toSort() = "$defaultRoundId-$defaultRoundSubtypeId"

        val duplicates = ClassificationRound
                .entries
                .sortedBy { it.rounds.toSort() }
                .fold<ClassificationRound, Pair<ClassificationRound?, List<ClassificationRound>>>(
                        null to listOf(),
                ) { (prev, acc), it ->
                    it to if (it.rounds == prev?.rounds && it.isCompound == prev.isCompound) (acc + it + prev) else acc
                }
                .second
                .distinct()

        println(duplicates)
        assertEquals(
                0,
                duplicates.size,
        )
    }

    @Test
    fun testMensYork() = runTest {
        val classificationTables = RawResourcesHelper.classificationTables
        val round = RawResourcesHelper.getDefaultRounds().find { it.round.displayName == "York / Hereford" }!!

        val entries = classificationTables.get(
                isGent = true,
                age = SENIOR,
                bow = RECURVE,
                fullRoundInfo = round,
                roundSubTypeId = 1,
                isTripleFace = false,
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
        val classificationTables = RawResourcesHelper.classificationTables
        val round = RawResourcesHelper.getDefaultRounds().find { it.round.displayName == "York / Hereford" }!!

        val entries = classificationTables.get(
                isGent = false,
                age = SENIOR,
                bow = RECURVE,
                fullRoundInfo = round,
                roundSubTypeId = 2,
                isTripleFace = false,
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
        val classificationTables = RawResourcesHelper.classificationTables
        val round = RawResourcesHelper.getDefaultRounds().find { it.round.displayName == "WA 1440 / FITA" }!!

        val entries = classificationTables.get(
                isGent = true,
                age = U15,
                bow = COMPOUND,
                fullRoundInfo = round,
                roundSubTypeId = 1,
                isTripleFace = false,
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
        val classificationTables = RawResourcesHelper.classificationTables
        val rounds = RawResourcesHelper.getDefaultRounds().associateBy { it.round.defaultRoundId }

        ClassificationBow.entries.forEach { bow ->
            ClassificationAge.entries.forEach { age ->
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
                                            isTripleFace = false,
                                    )!!.sortedByDescending { it.handicap }
                            if (actualEntries.isEmpty()) return@loop

                            val expectedEntries =
                                    classificationTables.getRoughHandicaps(
                                            isGent = isGent,
                                            age = age,
                                            bow = bow,
                                            wa1440RoundInfo = RoundPreviewHelper.wa1440RoundData,
                                            wa18RoundInfo = RoundPreviewHelper.wa18RoundData,
                                            isOutdoor = round.round.isOutdoor,
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

    /**
     * All rounds except Combined WA should have
     * all official classifications except [Classification.ELITE_MASTER_BOWMAN]
     */
    @Test
    fun testAllClassificationsAreOfficialForIndoorRounds() = runTest {
        val classificationTables = RawResourcesHelper.classificationTables
        val rounds = RawResourcesHelper.getDefaultRounds()
                .filter { !it.round.isOutdoor && it.round.defaultRoundId != RoundRepo.COMBINED_WA_DEFAULT_ROUND_ID }
                .associateBy { it.round.defaultRoundId }

        ClassificationBow.entries.forEach { bow ->
            ClassificationAge.entries.forEach { age ->
                listOf(true, false).forEach { isGent ->
                    rounds.forEach { (_, round) ->
                        (round.roundSubTypes?.map { it.subTypeId } ?: listOf(1)).forEach loop@{ subTypeId ->
                            val actualEntries =
                                    classificationTables.get(
                                            isGent = isGent,
                                            age = age,
                                            bow = bow,
                                            fullRoundInfo = round,
                                            roundSubTypeId = subTypeId,
                                            isTripleFace = false,
                                    )!!
                                            .map { it.classification }
                                            .sortedBy { it.ordinal }
                            assertEquals(
                                    Classification.entries.minus(Classification.ELITE_MASTER_BOWMAN),
                                    actualEntries,
                            )
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
