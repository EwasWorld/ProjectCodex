package eywa.projectcodex.databaseTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import eywa.projectcodex.common.TestUtils
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addFullSetOfArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.completeRound
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.joinToPrevious
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.setDate
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import eywa.projectcodex.databaseTests.DatabaseTestUtils.add
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
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ViewsTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: ScoresRoomDatabase

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
        val round = RoundPreviewHelper.indoorMetricRoundData
        val fullSetSum = TestUtils.ARROWS.sumOf { it.score }
        val date = Calendar.getInstance()

        db.add(round)
        val shoots = listOf(
                ShootPreviewHelper.newFullShootInfo(1).addRound(round).completeRound(10, false) to
                        ShootWithScore(ShootPreviewHelper.newShoot(), 600, round.round.roundId, 1, true, date),
                ShootPreviewHelper.newFullShootInfo(3).addRound(round).addFullSetOfArrows() to
                        ShootWithScore(ShootPreviewHelper.newShoot(), fullSetSum, round.round.roundId, 1, false, date),
                ShootPreviewHelper.newFullShootInfo(4).addFullSetOfArrows() to
                        ShootWithScore(ShootPreviewHelper.newShoot(), fullSetSum, null, 1, false, date),
                ShootPreviewHelper.newFullShootInfo(5).addFullSetOfArrows().setDate(joinedDate) to
                        ShootWithScore(ShootPreviewHelper.newShoot(), fullSetSum, null, 1, false, joinedDate),
                ShootPreviewHelper.newFullShootInfo(6).addFullSetOfArrows().setDate(secondDate).joinToPrevious() to
                        ShootWithScore(ShootPreviewHelper.newShoot(), fullSetSum, null, 1, false, joinedDate),
        )
        shoots.forEach { db.add(it.first) }
        advanceUntilIdle()

        assertEquals(
                shoots.map {
                    val d = if (it.first.shoot.joinWithPrevious) joinedDate else it.first.shoot.dateShot
                    it.second.copy(shoot = it.first.shoot, joinedDate = d)
                }.toSet(),
                db.testViewDao().getShootWithScores().first().toSet(),
        )
    }

    @Test
    fun testGetPersonalBestTest() = runTest {
        db.add(RoundPreviewHelper.indoorMetricRoundData)
        db.add(RoundPreviewHelper.outdoorImperialRoundData)

        db.add(
                ShootPreviewHelper.newFullShootInfo(1)
                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                        .completeRound(10)
        )
        db.add(
                ShootPreviewHelper.newFullShootInfo(2)
                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                        .completeRound(20)
        )
        db.add(
                ShootPreviewHelper.newFullShootInfo(3)
                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                        .completeRound(20)
        )
        db.add(
                ShootPreviewHelper.newFullShootInfo(4)
                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                        .completeRound(20)
                        .addFullSetOfArrows()
        )
        db.add(
                ShootPreviewHelper.newFullShootInfo(5)
                        .addFullSetOfArrows()
        )
        db.add(
                ShootPreviewHelper.newFullShootInfo(6)
                        .addRound(RoundPreviewHelper.outdoorImperialRoundData)
                        .completeRound(300)
        )
        db.add(
                ShootPreviewHelper.newFullShootInfo(7)
                        .addRound(RoundPreviewHelper.outdoorImperialRoundData)
                        .completeRound(200)
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
