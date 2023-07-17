package eywa.projectcodex.components.newScore

import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import org.junit.Test
import org.mockito.kotlin.mock

// TODO Test
class NewScoreViewModelUnitTest {
    private lateinit var sut: NewScoreViewModel
    private val dbMock = MockScoresRoomDatabase()
    private val updateDefaultRoundsTask: UpdateDefaultRoundsTask = mock { }

    fun setup() {
//        sut = NewScoreViewModel(dbMock.db, updateDefaultRoundsTask)
    }

    @Test
    fun testClassInitialisation() {
//        setup()
    }
}
