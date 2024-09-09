package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.sharedUi.previewHelpers.ArrowScoresPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.ScoresRoomDatabaseImpl
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import eywa.projectcodex.hiltModules.LocalDatabaseModule.Companion.add
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ViewsTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabaseImpl

    @Before
    fun createDb() {
        db = DatabaseTestUtils.createDatabase()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testGetShootWithScore() = runTest {
        val joinedDate = Calendar.getInstance().apply { add(Calendar.MINUTE, 20) }
        val secondDate = Calendar.getInstance().apply { add(Calendar.MINUTE, 25) }
        val metricRound = RoundPreviewHelper.indoorMetricRoundData
        val date = Calendar.getInstance()

        fun FullShootInfo.toShootWithScore(joinedDate: Calendar) = ShootWithScore(
                shoot = shoot,
                score = arrows.orEmpty().sumOf { it.score },
                roundId = round?.roundId,
                nonNullSubTypeId = roundSubType?.subTypeId ?: 1,
                joinedDate = joinedDate,
                counterCount = arrowCounter?.shotCount,
                scoringArrowCount = arrows?.size ?: 0,
                roundCount = roundArrowCounts?.sumOf { it.arrowCount },
        )

        db.add(metricRound)
        val shoots = listOf(
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 1, dateShot = date)
                    round = metricRound
                    completeRound(10, false)
                }.let { it to it.toShootWithScore(date) },
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 3, dateShot = date)
                    round = metricRound
                    addFullSetOfArrows()
                }.let { it to it.toShootWithScore(date) },
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 4, dateShot = date)
                    addFullSetOfArrows()
                }.let { it to it.toShootWithScore(date) },
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 5, dateShot = joinedDate)
                }.let { it to it.toShootWithScore(joinedDate) },
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 6, dateShot = secondDate, joinWithPrevious = true)
                }.let { it to it.toShootWithScore(joinedDate) },
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 7, dateShot = date)
                    addArrowCounter(24)
                }.let { it to it.toShootWithScore(date) },
                ShootPreviewHelperDsl.create {
                    shoot = DatabaseShoot(shootId = 8, dateShot = date)
                    round = metricRound
                    completeRoundWithCounter()
                }.let { it to it.toShootWithScore(date) },
        )
        shoots.forEach { db.add(it.first) }
        advanceUntilIdle()

        assertEquals(
                shoots.map { it.second }.toSet(),
                db.testViewDao().getShootWithScores().first().toSet(),
        )
    }

    @Test
    fun testGetPersonalBestTest() = runTest {
        db.add(RoundPreviewHelper.indoorMetricRoundData)
        db.add(RoundPreviewHelper.outdoorImperialRoundData)

        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 1)
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(10)
                }
        )
        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 2)
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(20)
                }
        )
        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 3)
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(20)
                }
        )
        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 4)
                    round = RoundPreviewHelper.indoorMetricRoundData
                    completeRoundWithFinalScore(20)
                    addDbArrows(ArrowScoresPreviewHelper.getArrowsInOrderFullSet(shoot.shootId))
                }
        )
        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 5)
                    addFullSetOfArrows()
                }
        )
        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 6)
                    round = RoundPreviewHelper.outdoorImperialRoundData
                    completeRoundWithFinalScore(300)
                }
        )
        db.add(
                ShootPreviewHelperDsl.create {
                    shoot = shoot.copy(shootId = 7)
                    round = RoundPreviewHelper.outdoorImperialRoundData
                    completeRoundWithFinalScore(200)
                }
        )
        advanceUntilIdle()

        assertEquals(
                setOf(
                        PersonalBest(
                                RoundPreviewHelper.indoorMetricRoundData.round.roundId,
                                1,
                                20,
                                true,
                        ),
                        PersonalBest(
                                RoundPreviewHelper.outdoorImperialRoundData.round.roundId,
                                1,
                                300,
                                false,
                        ),
                ),
                db.testViewDao().getPbs().first().toSet(),
        )
    }
}
