package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton.GeneralTargetScoreButton
import eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton.WorcesterTargetScoreButton
import eywa.projectcodex.database.RoundFace
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrowButtonsUnitTest {
    @Test
    fun testGeneralTargetScoreButton() {
        listOf(
                GeneralTargetScoreButton.M,
                GeneralTargetScoreButton.SIX,
                GeneralTargetScoreButton.SEVEN,
                GeneralTargetScoreButton.EIGHT,
                GeneralTargetScoreButton.NINE,
                GeneralTargetScoreButton.TEN,
                GeneralTargetScoreButton.X,
        ).forEach { button ->
            RoundFace.values().forEach { face ->
                assertTrue("$button - $face", button.shouldShow(face))
            }
        }

        RoundFace.values().forEach { face ->
            val msg = "${GeneralTargetScoreButton.FIVE} - $face"
            if (face == RoundFace.FULL || face == RoundFace.FITA_SIX) {
                assertTrue(msg, GeneralTargetScoreButton.FIVE.shouldShow(face))
            }
            else {
                assertFalse(msg, GeneralTargetScoreButton.FIVE.shouldShow(face))
            }
        }

        listOf(
                GeneralTargetScoreButton.FOUR,
                GeneralTargetScoreButton.THREE,
                GeneralTargetScoreButton.TWO,
                GeneralTargetScoreButton.ONE,
        ).forEach { button ->
            RoundFace.values().forEach { face ->
                val msg = "$button - $face"
                if (face == RoundFace.FULL) {
                    assertTrue(msg, button.shouldShow(face))
                }
                else {
                    assertFalse(msg, button.shouldShow(face))
                }
            }
        }
    }

    @Test
    fun testWorcesterTargetScoreButton() {
        listOf(
                WorcesterTargetScoreButton.M,
                WorcesterTargetScoreButton.FIVE,
                WorcesterTargetScoreButton.FOUR,
        ).forEach { button ->
            RoundFace.values().forEach { face ->
                assertTrue("$button - $face", button.shouldShow(face))
            }
        }

        listOf(
                WorcesterTargetScoreButton.THREE,
                WorcesterTargetScoreButton.TWO,
                WorcesterTargetScoreButton.ONE,
        ).forEach { button ->
            RoundFace.values().forEach { face ->
                val msg = "$button - $face"
                if (face == RoundFace.FULL) {
                    assertTrue(msg, button.shouldShow(face))
                }
                else {
                    assertFalse(msg, button.shouldShow(face))
                }
            }
        }
    }
}
