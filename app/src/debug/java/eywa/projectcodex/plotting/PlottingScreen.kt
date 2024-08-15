package eywa.projectcodex.plotting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.plotting.PlottingEvent.*
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

data class ArrowWithLocation(
        val score: Int,

        /**
         * 0 is centre, 1 is a line cutter 1 (in the centre of the line).
         * >1 is allowed to score line cutter 1s or misses
         */
        val r: Double,

        /**
         * Angle in radians
         */
        val theta: Double,
) {
    fun getOffset(canvasRadius: Float) =
            Offset(
                    x = (canvasRadius * r * cos(theta)).toFloat(),
                    y = (canvasRadius * r * sin(theta)).toFloat(),
            )

    companion object {
        fun fromOffset(offset: Offset, canvasRadius: Float, isFiveZone: Boolean): ArrowWithLocation {
            val r = (offset.getDistance() / canvasRadius).toDouble()
            val score = if (r > 1) 0 else ceil((1 - r) * 10).toInt()
            val fiveZoneAdjust = if (isFiveZone && score % 2 == 0) 1 else 0

            return ArrowWithLocation(
                    score = score - fiveZoneAdjust,
                    r = r,
                    theta = atan2(offset.y, offset.x).toDouble(),
            )
        }
    }
}

val arrows = listOf(
        ArrowWithLocation(
                score = 7,
                r = 0.4,
                theta = 0.0,
        ),
        ArrowWithLocation(
                score = 9,
                r = 0.2,
                theta = 0.5,
        ),
)

val movingArrowInit =
        ArrowWithLocation(
                score = 10,
                r = 0.0,
                theta = Math.PI / 2,
        )

data class PlottingState(
        val fixedArrows: List<ArrowWithLocation> = arrows,
        val movingArrow: ArrowWithLocation = movingArrowInit,
        val sensitivityIndex: Int = Sensitivity.NORMAL.ordinal,
) {
    val arrows
        get() = fixedArrows + movingArrow

    val sensitivity = Sensitivity.entries[sensitivityIndex]

    val isMaxSensitivity
        get() = sensitivityIndex == Sensitivity.entries.lastIndex
    val isMinSensitivity
        get() = sensitivityIndex == 0
}

enum class Sensitivity(val multiplier: Float) {
    LOW(0.3f),
    NORMAL(1f),
}

sealed class PlottingEvent {
    data class MoveArrow(val arrow: ArrowWithLocation) : PlottingEvent()
    data object CompleteArrow : PlottingEvent()
    data object IncreaseSensitivity : PlottingEvent()
    data object DecreaseSensitivity : PlottingEvent()
}

@Composable
fun PlottingScreen() {
    var state by remember { mutableStateOf(PlottingState()) }

    PlottingScreen({ state }) {
        state = when (it) {
            is MoveArrow -> state.copy(movingArrow = it.arrow)
            CompleteArrow -> {
                state.copy(fixedArrows = state.fixedArrows + state.movingArrow, movingArrow = movingArrowInit)
            }

            IncreaseSensitivity ->
                state.copy(sensitivityIndex = (state.sensitivityIndex + 1).coerceIn(Sensitivity.entries.indices))

            DecreaseSensitivity ->
                state.copy(sensitivityIndex = (state.sensitivityIndex - 1).coerceIn(Sensitivity.entries.indices))
        }
    }
}

@Composable
fun PlottingScreen(
        state: () -> PlottingState,
        listener: (PlottingEvent) -> Unit,
) {
    BoxWithConstraints {
        DrawTargetAndPlotArrows(state, listener)


        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(CodexTheme.colors.appBackground)
        ) {
            val arrows = state().fixedArrows.plus(state().movingArrow)
            arrows.forEach {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                            text = "10",
                            color = Color.Transparent,
                            modifier = Modifier
                                    .background(CodexTheme.colors.getColourForArrowValue(it.score), CircleShape)
                    )
                    Text(
                            text = it.score.toString(),
                            color = CodexTheme.colors.getTextColourForArrowValue(it.score),
                    )
                }
            }
        }
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(CodexTheme.colors.appBackground)
        ) {
            CodexButton(text = "Next") { listener(CompleteArrow) }
            CodexButton(text = if (state().isMinSensitivity) "Increase\nsensitivity" else "Decrease\nsensitivity") {
                listener(if (state().isMinSensitivity) IncreaseSensitivity else DecreaseSensitivity)
            }
        }
    }
}
