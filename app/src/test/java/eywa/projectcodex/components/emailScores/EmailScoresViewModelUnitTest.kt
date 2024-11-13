package eywa.projectcodex.components.emailScores

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import eywa.projectcodex.testUtils.MainCoroutineRule
import eywa.projectcodex.testUtils.MockDatastore
import eywa.projectcodex.testUtils.MockScoresRoomDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class EmailScoresViewModelUnitTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun testEmailInsertion() = runTest {
        fun Pair<String, Int>.toTextFieldValue() = TextFieldValue(first, TextRange(second))

        val testEmail = "testemail@gmail.com"

        val tests = mapOf(
                ("" to 0) to (testEmail to testEmail.length),

                ("hello" to 0) to (testEmail to testEmail.length),
                ("hello" to 1) to (testEmail to testEmail.length),
                ("hello" to 5) to (testEmail to testEmail.length),
                ("hello" to 100) to (testEmail to testEmail.length),

                (",ggg" to 0) to ("$testEmail,ggg" to testEmail.length),
                ("hello,ggg" to 0) to ("$testEmail,ggg" to testEmail.length),
                ("hello,ggg" to 1) to ("$testEmail,ggg" to testEmail.length),
                ("hello,ggg" to 5) to ("$testEmail,ggg" to testEmail.length),

                ("ggg,,ggg" to 4) to ("ggg,$testEmail,ggg" to testEmail.length + 4),
                ("ggg,hello,ggg" to 4) to ("ggg,$testEmail,ggg" to testEmail.length + 4),
                ("ggg,hello,ggg" to 5) to ("ggg,$testEmail,ggg" to testEmail.length + 4),
                ("ggg,hello,ggg" to 9) to ("ggg,$testEmail,ggg" to testEmail.length + 4),

                ("ggg,hello" to 4) to ("ggg,$testEmail" to testEmail.length + 4),
                ("ggg,hello" to 100) to ("ggg,$testEmail" to testEmail.length + 4),
        )

        tests.forEach { (input, output) ->
            val sut = EmailScoresViewModel(
                    context = mock {},
                    datastore = MockDatastore().mock,
                    helpShowcase = mock {},
                    db = MockScoresRoomDatabase().mock,
                    shootIdsUseCase = mock {
                        onBlocking { getItems } doReturn MutableStateFlow(emptyList())
                    },
                    customLogger = mock {},
            )

            sut.handle(EmailScoresIntent.UpdateEmail(input.toTextFieldValue()))
            sut.handle(EmailScoresIntent.InsertEmail(testEmail))

            assertEquals("${input.first} ${input.second}", output.toTextFieldValue(), sut.state.value.emailField)
        }
    }
}
