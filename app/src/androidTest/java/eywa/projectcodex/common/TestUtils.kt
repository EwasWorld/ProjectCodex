package eywa.projectcodex.common

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.model.Arrow
import org.mockito.ArgumentCaptor
import java.util.Calendar
import kotlin.random.Random

typealias ComposeTestRule<T> = AndroidComposeTestRule<ActivityScenarioRule<T>, T>

object TestUtils {
    const val ARROW_PLACEHOLDER = "."
    const val ARROW_DELIMINATOR = "-"

    val ARROWS = listOf(
            Arrow(0, false),
            Arrow(1, false),
            Arrow(2, false),
            Arrow(3, false),
            Arrow(4, false),
            Arrow(5, false),
            Arrow(6, false),
            Arrow(7, false),
            Arrow(8, false),
            Arrow(9, false),
            Arrow(10, false),
            Arrow(10, true),
    )

    val ROUNDS = listOf(
            FullRoundInfo(
                    round = Round(1, "wa 1440", "WA 1440", true, true, false, null, null),
                    roundArrowCounts = listOf(
                            RoundArrowCount(1, 1, 122.0, 48),
                            RoundArrowCount(1, 2, 122.0, 36),
                            RoundArrowCount(1, 3, 60.0, 24),
                    ),
                    roundSubTypes = listOf(
                            RoundSubType(1, 1, "1-1", null, null),
                            RoundSubType(1, 2, "1-2", 12, 14),
                            RoundSubType(1, 3, "1-3", 0, 0),
                    ),
                    roundDistances = listOf(
                            RoundDistance(1, 1, 1, 90),
                            RoundDistance(1, 2, 1, 80),
                            RoundDistance(1, 3, 1, 70),
                            RoundDistance(1, 1, 2, 80),
                            RoundDistance(1, 2, 2, 70),
                            RoundDistance(1, 3, 2, 60),
                            RoundDistance(1, 1, 3, 70),
                            RoundDistance(1, 2, 3, 60),
                            RoundDistance(1, 3, 3, 50),
                    ),
            ),
            FullRoundInfo(
                    round = Round(2, "st george", "St. George", true, false, false, null, null),
                    roundArrowCounts = listOf(
                            RoundArrowCount(2, 1, 922.0, 60),
                            RoundArrowCount(2, 2, 922.0, 48),
                            RoundArrowCount(2, 3, 90.0, 36),
                    ),
                    roundSubTypes = listOf(
                            RoundSubType(2, 1, "2-1", null, null),
                    ),
                    roundDistances = listOf(
                            RoundDistance(2, 1, 1, 100),
                            RoundDistance(2, 2, 1, 80),
                            RoundDistance(2, 3, 1, 60),
                    ),
            ),
            FullRoundInfo(
                    round = Round(3, "portsmouth", "Portsmouth", false, false, false, null, null),
                    roundArrowCounts = listOf(
                            RoundArrowCount(3, 1, 822.0, 70),
                            RoundArrowCount(3, 2, 822.0, 60),
                            RoundArrowCount(3, 3, 80.0, 50),
                    ),
                    roundSubTypes = listOf(
                            RoundSubType(3, 1, "3-1", null, null),
                            RoundSubType(3, 2, "3-2", null, null),
                    ),
                    roundDistances = listOf(
                            RoundDistance(3, 1, 1, 50),
                            RoundDistance(3, 2, 1, 30),
                            RoundDistance(3, 1, 2, 20),
                            RoundDistance(3, 2, 2, 10),
                    ),
            ),
            FullRoundInfo(
                    round = Round(4, "wa25", "WA 25", false, true, false, null, null),
                    roundArrowCounts = listOf(
                            RoundArrowCount(4, 1, 60.0, 60),
                    ),
                    roundDistances = listOf(
                            RoundDistance(4, 1, 1, 25),
                    ),
            ),
    )

    /**
     * @return a valid date in the given year (will never return 31st of a month or 29th Feb), time 00:00
     */
    fun generateDate(year: Int = 2019, month: Int? = null): Calendar {
        val generatedMonth = month ?: (Random.nextInt(12) + 1)
        val day = 1 + Random.nextInt(
                when (month) {
                    2 -> 28
                    9, 4, 6, 11 -> 30
                    else -> 31
                }
        )
        return DateTimeFormat.SHORT_DATE.parse("$day/$generatedMonth/$year")
    }

    fun String.parseDate(format: DateTimeFormat = DateTimeFormat.SHORT_DATE_TIME) = format.parse(this)

    fun generateShoots(roundsToGenerate: Int): List<DatabaseShoot> = List(roundsToGenerate) { generateDate() }
            .sortedDescending()
            .mapIndexed { index, date ->
                DatabaseShoot(index + 1, date)
            }

    fun generateArrows(numberToGenerate: Int, desiredTotal: Int? = null): List<Arrow> {
        if (desiredTotal == null) {
            return List(numberToGenerate) { ARROWS[it % ARROWS.size] }
        }

        require(desiredTotal < numberToGenerate * 10) { "Invalid desired total" }

        val tens = Math.floorDiv(desiredTotal, 10)
        return List(numberToGenerate) {
            when {
                it < tens -> Arrow(10, false)
                it == tens -> Arrow(desiredTotal % 10, false)
                else -> Arrow(0, false)
            }
        }
    }

    fun generateArrowScores(shootId: Int, numberToGenerate: Int, desiredTotal: Int? = null) =
            generateArrows(numberToGenerate, desiredTotal)
                    .mapIndexed { arrowIndex, arrow ->
                        arrow.asArrowScore(shootId, arrowIndex + 1)

                    }

    /**
     * Use this when the usual argumentCaptor.capture() apparently throws a null
     *
     * Source:
     * - https://stackoverflow.com/a/46064204
     * - https://github.com/android/architecture-components-samples/blob/master/BasicRxJavaSampleKotlin/app/src/test/java/com/example/android/observability/MockitoKotlinHelpers.kt
     *
     * @return ArgumentCaptor.capture() as nullable type to avoid java.lang.IllegalStateException when null is
     * returned
     */
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
}
