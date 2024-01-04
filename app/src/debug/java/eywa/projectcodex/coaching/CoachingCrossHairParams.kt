package eywa.projectcodex.coaching

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class CoachingCrossHairParams(
        private val x: Float,
        private val y: Float,
        /**
         * Rotation of line anticlockwise from horizontal in radians
         */
        private val rotationRads: Float,
        /**
         * Distance from the point ([x],[y]) along the vertical like ([rotationRads] + PI/2).
         * Positive means below the point (down in the y direction)
         */
        private val distance: Float,
        val mode: CoachingCrossHairMode = CoachingCrossHairMode.SET_FEET,
) {
    private val inverseAngle = rotationRads + Math.PI.toFloat() / 2

    /**
     * Line that should run through centre of both feet.
     * Runs through ([x],[y]) with an angle of [rotationRads]
     */
    fun getFeetLine(size: Size) = getLineStartAndEnd(x, y, (rotationRads), size)

    /**
     * Line that should run vertically through the centre of the body.
     * When standing upright, goes from the centre of the head, through the centre of the body and hips,
     * and to the mid-point between the feet.
     * Runs through ([x],[y]) with an angle of [inverseAngle]
     */
    fun getBodyLine(size: Size) = getLineStartAndEnd(x, y, (inverseAngle), size)

    /**
     * Line that should run through the centre of the shoulders.
     * Runs through ([x],[y]) offset by [distance] with an angle of [rotationRads]
     */
    fun getShoulderLine(size: Size) =
            getAltXY().let { (newX, newY) -> getLineStartAndEnd(newX, newY, (rotationRads), size) }

    /**
     * ([x],[y]) offset by [distance]
     */
    private fun getAltXY() = (x + cos(inverseAngle) * distance) to (y + sin(inverseAngle) * distance)

    /**
     * Min and max of [distance]
     */
    private fun getDistanceBounds(size: Size) = getBodyLine(size).toList().map {
        val c = getHypotenuse(it.x - x, it.y - y)
        val mult = if (it.y < y) -1 else 1
        c * mult
    }.sorted()[0] to 10f

    fun plus(pan: Offset, rotation: Float, maxSize: Size) = when (mode) {
        CoachingCrossHairMode.SET_FEET -> {
            CoachingCrossHairParams(
                    x = (x + pan.x).coerceIn(0f, maxSize.width),
                    y = (y + pan.y).coerceIn(0f, maxSize.height),
                    rotationRads = (rotationRads + rotation).coerceIn(-Math.PI.toFloat() / 4, Math.PI.toFloat() / 4),
                    distance = distance,
                    mode = CoachingCrossHairMode.SET_FEET,
            )
        }

        CoachingCrossHairMode.SET_SHOULDERS -> {
            val panMagnitude = getHypotenuse(pan.x, pan.y)

            if (panMagnitude.isEqualTo(0f)) {
                this
            }
            else {
                val panAngle = when {
                    pan.x.isInRange(0f, 0f) && pan.y <= 0f -> 3 * Math.PI.toFloat() / 2
                    pan.x.isInRange(0f, 0f) -> Math.PI.toFloat() / 2
                    else -> atan2(pan.y, pan.x)
                }
                val distanceChange = panMagnitude * cos(inverseAngle - panAngle)

                val (minDistance, maxDistance) = getDistanceBounds(maxSize)
                CoachingCrossHairParams(
                        x = x,
                        y = y,
                        rotationRads = rotationRads,
                        distance = (distance + distanceChange).coerceIn(minDistance, maxDistance),
                        mode = mode,
                )
            }
        }
    }

    fun nextMode(maxSize: Size): CoachingCrossHairParams? {
        val nextMode = mode.next() ?: return null
        return shiftMode(nextMode, maxSize)
    }

    fun previousMode(maxSize: Size): CoachingCrossHairParams? {
        val nextMode = mode.previous() ?: return null
        return shiftMode(nextMode, maxSize)
    }

    private fun shiftMode(nextMode: CoachingCrossHairMode, maxSize: Size): CoachingCrossHairParams {
        val (minDistance, maxDistance) = getDistanceBounds(maxSize)
        val newDistance =
                if (nextMode != CoachingCrossHairMode.SET_SHOULDERS) distance
                else distance.coerceIn(minDistance, maxDistance)

        return CoachingCrossHairParams(
                x = x,
                y = y,
                rotationRads = rotationRads,
                distance = newDistance,
                mode = nextMode,
        )
    }

    fun Float.toDeg() = 360 * this / (2 * Math.PI.toFloat())

    companion object {
        fun getLineStartAndEnd(
                x: Float = 0f,
                y: Float = 0f,
                rotationRads: Float = 0f,
                size: Size,
        ): Pair<Offset, Offset> {
            if ((rotationRads % Math.PI.toFloat()).isEqualTo(Math.PI.toFloat() / 2)) {
                return Offset(x, 0f) to Offset(x, size.height)
            }

            // y = mx + c
            val m = tan(rotationRads)
            val c = (y - m * x)

            // Find points that intercept with lines x = 0 and x = size.width (same for height)
            // Then remove ones outside of size, leaving just the start/end of the line
            val points = listOf(
                    Offset(0f, (m * 0 + c)),
                    Offset(size.width, (m * size.width + c)),
                    Offset(((0 - c) / m), 0f),
                    Offset(((size.height - c) / m), size.height),
            )
                    .filter {
                        it.x.isInRange(size.width, 0f) && it.y.isInRange(size.height, 0f)
                    }
                    .distinct()
                    .sortedBy { it.x }
                    .toMutableList()

            var i = 1
            while (points.size > 2 && i in points.indices) {
                val current = points[i - 1]
                val next = points[i]
                if (current.x.isEqualTo(next.x) && current.y.isEqualTo(next.y)) {
                    points.removeAt(i)
                    continue
                }
                i++
            }

            if (points.size != 2) {
                throw IllegalStateException("Invalid line $size\n$points")
            }

            return points[0] to points[1]
        }

        private fun Float.isEqualTo(value: Float, delta: Float = 0.01f) =
                this.isInRange(value, value, delta)

        private fun Float.isInRange(max: Float, min: Float, delta: Float = 0.01f) =
                this >= (min - delta) && this <= (max + delta)
    }
}
