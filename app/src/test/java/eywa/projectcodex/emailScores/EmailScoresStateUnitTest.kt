package eywa.projectcodex.emailScores

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import eywa.projectcodex.components.emailScores.EmailScoresState
import org.junit.Assert.assertEquals
import org.junit.Test

class EmailScoresStateUnitTest {
    @Test
    fun testCurrentlyTypingEmail() {
        val testEmail = "abc,def,ghi"

        repeat(testEmail.length + 1) {
            val state = EmailScoresState(emailField = TextFieldValue(testEmail, TextRange(it)))
            assertEquals(
                    "Cursor: $it",
                    when {
                        it < 4 -> "abc"
                        it < 8 -> "def"
                        else -> "ghi"
                    },
                    state.currentlyTypingEmail(),
            )
        }
    }
}
