package eywa.projectcodex.components

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseState
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.mainMenu.AppVersion
import eywa.projectcodex.components.mainMenu.MainMenuIntent
import eywa.projectcodex.components.mainMenu.MainMenuState
import eywa.projectcodex.components.mainMenu.MainMenuViewModel
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainMenuViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var helpShowcase: HelpShowcaseUseCase
    private val datastore = MockDatastore()

    private val defaultState = MainMenuState(whatsNewDialogOpen = true)

    private fun getSut(
            helpShowcaseInProgress: Boolean = false,
    ): MainMenuViewModel {
        helpShowcase = mock {
            on { state } doReturn flow { emit(HelpShowcaseState(isInProgress = helpShowcaseInProgress)) }
        }
        return MainMenuViewModel(helpShowcase, datastore.mock)
    }

    @Test
    fun testInitialisation_WhatsNewInfo_Empty() = runTest {
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                MainMenuState(
                        whatsNewDialogOpen = true,
                        whatsNewDialogLastSeenAppVersion = null,
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_WhatsNewInfo_EmptyWithDisplayedHandicapNotification() = runTest {
        datastore.values = mapOf(DatastoreKey.DisplayHandicapNotice to false)
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                MainMenuState(
                        whatsNewDialogOpen = true,
                        whatsNewDialogLastSeenAppVersion = AppVersion("2.1.0"),
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_WhatsNewInfo_EarlyAppVersion() = runTest {
        datastore.values = mapOf(
                DatastoreKey.WhatsNewLastOpenedAppVersion to "2.0.0",
                DatastoreKey.DisplayHandicapNotice to false,
        )
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                MainMenuState(
                        whatsNewDialogOpen = true,
                        whatsNewDialogLastSeenAppVersion = AppVersion("2.0.0"),
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_WhatsNewInfo_LateAppVersion() = runTest {
        datastore.values = mapOf(DatastoreKey.WhatsNewLastOpenedAppVersion to "10.0.0")
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                MainMenuState(
                        whatsNewDialogOpen = false,
                        whatsNewDialogLastSeenAppVersion = AppVersion("10.0.0"),
                ),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_BetaFeatures() = runTest {
        datastore.values = mapOf(DatastoreKey.UseBetaFeatures to true)
        val sut = getSut()
        advanceUntilIdle()

        assertEquals(
                defaultState.copy(useBetaFeatures = true),
                sut.state.value,
        )
    }

    @Test
    fun testInitialisation_HelpShowcaseInProgress() = runTest {
        val sut = getSut(helpShowcaseInProgress = true)
        advanceUntilIdle()

        assertEquals(
                defaultState.copy(isHelpShowcaseInProgress = true),
                sut.state.value,
        )
    }

    @Test
    fun testWhatsNewOpenAndClose() = runTest {
        val sut = getSut()
        advanceUntilIdle()
        assertEquals(MainMenuState(whatsNewDialogOpen = true), sut.state.value)

        sut.handle(MainMenuIntent.WhatsNewClose(AppVersion("1.0.0")))
        assertEquals(
                MainMenuState(
                        whatsNewDialogOpen = false,
                        whatsNewDialogLastSeenAppVersion = AppVersion("1.0.0"),
                ),
                sut.state.value,
        )
        advanceUntilIdle()
        verify(datastore.mock).set(DatastoreKey.WhatsNewLastOpenedAppVersion, "1.0.0")

        sut.handle(MainMenuIntent.WhatsNewOpen)
        assertEquals(
                MainMenuState(
                        whatsNewDialogOpen = true,
                        whatsNewDialogLastSeenAppVersion = AppVersion("1.0.0"),
                ),
                sut.state.value,
        )
        verify(datastore.mock).set(eq(DatastoreKey.WhatsNewLastOpenedAppVersion), any())

    }

    @Test
    fun testExitDialogIntents() {
        val sut = getSut()
        assertEquals(MainMenuState(), sut.state.value)

        sut.handle(MainMenuIntent.OpenExitDialog)
        assertEquals(
                MainMenuState(isExitDialogOpen = true),
                sut.state.value,
        )

        sut.handle(MainMenuIntent.ExitDialogCloseClicked)
        assertEquals(
                MainMenuState(),
                sut.state.value,
        )

        sut.handle(MainMenuIntent.OpenExitDialog)
        assertEquals(
                MainMenuState(isExitDialogOpen = true),
                sut.state.value,
        )

        sut.handle(MainMenuIntent.ExitDialogOkClicked)
        assertEquals(
                MainMenuState(closeApplication = true),
                sut.state.value,
        )

        sut.handle(MainMenuIntent.CloseApplicationHandled)
        assertEquals(
                MainMenuState(),
                sut.state.value,
        )
    }

    @Test
    fun testNavigate() {
        val sut = getSut()
        assertEquals(MainMenuState(), sut.state.value)

        sut.handle(MainMenuIntent.Navigate(CodexNavRoute.VIEW_SCORES))
        assertEquals(
                MainMenuState(navigateTo = CodexNavRoute.VIEW_SCORES),
                sut.state.value,
        )

        sut.handle(MainMenuIntent.NavigateHandled)
        assertEquals(
                MainMenuState(),
                sut.state.value,
        )
    }

    @Test
    fun testHelpShowcaseAction() {
        val sut = getSut()
        assertEquals(MainMenuState(), sut.state.value)

        sut.handle(MainMenuIntent.HelpShowcaseAction(HelpShowcaseIntent.Clear))
        assertEquals(MainMenuState(), sut.state.value)
        verify(helpShowcase).handle(HelpShowcaseIntent.Clear, CodexNavRoute.MAIN_MENU::class)
    }
}
