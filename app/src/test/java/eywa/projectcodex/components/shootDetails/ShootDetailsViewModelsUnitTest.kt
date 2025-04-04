package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.components.shootDetails.addEnd.AddEndExtras
import eywa.projectcodex.components.shootDetails.addEnd.AddEndIntent
import eywa.projectcodex.components.shootDetails.addEnd.AddEndState
import eywa.projectcodex.components.shootDetails.addEnd.AddEndViewModel
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.editEnd.EditEndExtras
import eywa.projectcodex.components.shootDetails.editEnd.EditEndIntent
import eywa.projectcodex.components.shootDetails.editEnd.EditEndState
import eywa.projectcodex.components.shootDetails.editEnd.EditEndViewModel
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndExtras
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndIntent
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndState
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndViewModel
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadExtras
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadIntent
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadState
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadViewModel
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.arrows.ArrowScoresRepo
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.user.CodexUserPreviewHelper
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.TestUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ShootDetailsViewModelsUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val mockArrowScoresRepo = mock<ArrowScoresRepo> { }
    private val mockDb = mock<ScoresRoomDatabase> {
        on { arrowScoresRepo() } doReturn mockArrowScoresRepo
    }
    private val helpShowcase = mock<HelpShowcaseUseCase> { }

    private val jobs = mutableListOf<Job>()

    private var shootDetailsState = ShootDetailsState(
            shootId = 1,
            fullShootInfo = ShootPreviewHelperDsl.create { addIdenticalArrows(36, 7) },
            scorePadSelectedEnd = 1,
            user = CodexUserPreviewHelper.allCapabilities,
    )

    @Suppress("UNCHECKED_CAST")
    private fun <S : Any, E : Any> setupRepo() = mock<ShootDetailsRepo> {
        on {
            getState(any(), any<(ShootDetailsState, E) -> S>())
        } doAnswer {
            val converter = it.arguments[1] as (ShootDetailsState, E) -> S
            flow {
                delay(TestUtils.FLOW_EMIT_DELAY)
                emit(shootDetailsState)
            }.combine(it.arguments[0] as StateFlow<E>) { mainA, extra ->
                ShootDetailsResponse.Loaded(
                        data = converter(mainA, extra),
                        shootId = 1,
                        navBarClicked = null,
                        backClicked = false,
                        isCounting = mainA.fullShootInfo!!.arrowCounter != null,
                )
            }
        }

        on { db } doReturn mockDb
    }

    private fun teardown() {
        jobs.forEach { it.cancel() }
    }

    private fun <S : Any> TestScope.collectState(
            flow: StateFlow<ShootDetailsResponse<S>>,
            states: MutableList<ShootDetailsResponse<S>>,
    ) {
        jobs.add(launch { flow.collect { states.add(it) } })

        // Ignore the first loading state
        advanceTimeBy(1)
        states.clear()
    }


    @Test
    fun testAddEndViewModel_handleArrowInputIntent() = runTest {
        val arrows = List(6) { Arrow(it) }
        shootDetailsState = shootDetailsState.copy(addEndArrows = arrows)
        val initialState = AddEndState(
                main = shootDetailsState,
                extras = AddEndExtras(),
        )

        val repoMock = setupRepo<AddEndState, AddEndExtras>()
        val sut = AddEndViewModel(repoMock, helpShowcase)
        val states = mutableListOf<ShootDetailsResponse<AddEndState>>()
        collectState(sut.state, states)
        advanceUntilIdle()
        assertEquals(
                initialState,
                states.last().getData(),
        )

        states.clear()
        sut.handle(AddEndIntent.ArrowInputsAction(ArrowInputsIntent.SubmitClicked))
        advanceUntilIdle()

        verify(mockArrowScoresRepo).insert(
                *arrows
                        .mapIndexed { index, arrow -> arrow.asArrowScore(1, index + 37) }
                        .toTypedArray(),
        )
        verify(repoMock)
                .handle(ShootDetailsIntent.SetInputtedArrows(emptyList()), CodexNavRoute.SHOOT_DETAILS_ADD_END)

        teardown()
    }

    @Test
    fun testEditEndViewModel_handleArrowInputIntent() = runTest {
        shootDetailsState = shootDetailsState.copy(scorePadSelectedEnd = 2)
        val arrows = List(6) { Arrow(it) }
        val initialState = EditEndState(
                main = shootDetailsState,
                extras = EditEndExtras(enteredArrows = arrows),
        )

        val repoMock = setupRepo<EditEndState, EditEndExtras>()
        val sut = EditEndViewModel(repoMock, helpShowcase)
        val states = mutableListOf<ShootDetailsResponse<EditEndState>>()
        collectState(sut.state, states)

        advanceUntilIdle()
        sut.handle(EditEndIntent.ArrowInputsAction(ArrowInputsIntent.ClearArrowsInputted))
        advanceUntilIdle()
        repeat(6) {
            sut.handle(EditEndIntent.ArrowInputsAction(ArrowInputsIntent.ArrowInputted(Arrow(it))))
            advanceUntilIdle()
        }

        assertEquals(
                initialState,
                states.last().getData(),
        )

        states.clear()
        sut.handle(EditEndIntent.ArrowInputsAction(ArrowInputsIntent.SubmitClicked))
        advanceUntilIdle()

        verify(mockArrowScoresRepo).update(
                *arrows
                        .mapIndexed { index, arrow -> arrow.asArrowScore(1, index + 7) }
                        .toTypedArray(),
        )
        assertEquals(
                EditEndState(
                        main = shootDetailsState,
                        extras = EditEndExtras(enteredArrows = arrows, closeScreen = true),
                ),
                states.single().getData(),
        )

        teardown()
    }

    @Test
    fun testInsertEndViewModel_handleArrowInputIntent() = runTest {
        shootDetailsState = shootDetailsState.copy(scorePadSelectedEnd = 2)
        val arrows = List(6) { Arrow(it) }
        val initialState = InsertEndState(
                main = shootDetailsState,
                extras = InsertEndExtras(enteredArrows = arrows),
        )

        val repoMock = setupRepo<InsertEndState, InsertEndExtras>()
        val sut = InsertEndViewModel(repoMock, helpShowcase)
        val states = mutableListOf<ShootDetailsResponse<InsertEndState>>()
        collectState(sut.state, states)

        advanceUntilIdle()
        repeat(6) {
            sut.handle(InsertEndIntent.ArrowInputsAction(ArrowInputsIntent.ArrowInputted(Arrow(it))))
            advanceUntilIdle()
        }

        assertEquals(
                initialState,
                states.last().getData(),
        )

        states.clear()
        sut.handle(InsertEndIntent.ArrowInputsAction(ArrowInputsIntent.SubmitClicked))
        advanceUntilIdle()

        verify(mockArrowScoresRepo).insertEnd(
                initialState.fullShootInfo.arrows!!,
                arrows.mapIndexed { index, arrow -> arrow.asArrowScore(1, index + 7) },
        )
        assertEquals(
                InsertEndState(
                        main = shootDetailsState,
                        extras = InsertEndExtras(enteredArrows = arrows, closeScreen = true),
                ),
                states.single().getData(),
        )

        teardown()
    }

    @Test
    fun testScorePadViewModel_deleteEnd() = runTest {
        val initialState = ScorePadState(
                main = shootDetailsState,
                extras = ScorePadExtras(deleteEndDialogIsShown = true),
        )

        val repoMock = setupRepo<ScorePadState, ScorePadExtras>()
        val sut = ScorePadViewModel(repoMock, helpShowcase)
        sut.handle(ScorePadIntent.DeleteEndClicked(1))
        val states = mutableListOf<ShootDetailsResponse<ScorePadState>>()
        collectState(sut.state, states)

        advanceUntilIdle()
        assertEquals(
                initialState,
                states.single().getData(),
        )

        states.clear()
        sut.handle(ScorePadIntent.DeleteEndDialogOkClicked)
        advanceUntilIdle()

        verify(mockArrowScoresRepo).deleteEnd(
                initialState.arrows!!,
                initialState.firstArrowNumberInSelectedEnd!!,
                initialState.selectedEndSize!!,
        )
        verify(repoMock)
                .handle(ShootDetailsIntent.SelectScorePadEnd(null), CodexNavRoute.SHOOT_DETAILS_SCORE_PAD)
        assertEquals(
                ScorePadState(main = shootDetailsState, extras = ScorePadExtras()),
                states.single().getData(),
        )

        teardown()
    }
}
