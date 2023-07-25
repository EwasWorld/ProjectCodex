package eywa.projectcodex.common.utils.updateDefaultRounds

import android.content.res.Resources
import eywa.projectcodex.R

sealed class UpdateDefaultRoundsState {
    open val databaseVersion: Int? = null
    open val hasTaskFinished = true
    open fun asLogString() = this::class.simpleName
    abstract fun asDisplayString(resources: Resources): String

    object NotStarted : UpdateDefaultRoundsState() {
        override val hasTaskFinished = false

        override fun asLogString(): String = "Not started"

        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.about__update_default_rounds_not_started)
    }

    object Initialising : UpdateDefaultRoundsState() {
        override val databaseVersion: Int? = null
        override val hasTaskFinished = false
        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.about__update_default_rounds_initialising)
    }

    /**
     * @param currentItemIndex 1-indexed
     */
    data class StartProcessingNew(
            override val databaseVersion: Int?, val currentItemIndex: Int, val totalItems: Int
    ) : UpdateDefaultRoundsState() {
        override val hasTaskFinished = false
        override fun asLogString(): String = "Processing $currentItemIndex of $totalItems"
        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.about__update_default_rounds_processing, currentItemIndex, totalItems)
    }

    data class DeletingOld(override val databaseVersion: Int?) : UpdateDefaultRoundsState() {
        override val hasTaskFinished = false
        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.about__update_default_rounds_deleting)
    }

    data class Complete(override val databaseVersion: Int?, val type: CompletionType) : UpdateDefaultRoundsState() {
        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.about__update_default_rounds_up_to_date)
    }

    data class InternalError(override val databaseVersion: Int?, val message: String) : UpdateDefaultRoundsState() {
        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.err_about__update_default_rounds_failed)
    }

    data class TemporaryError(override val databaseVersion: Int?, val type: ErrorType) : UpdateDefaultRoundsState() {
        override fun asDisplayString(resources: Resources) = when (type) {
            ErrorType.CANT_ACQUIRE_LOCK -> resources.getString(R.string.err_about__update_default_rounds_no_lock)
        }
    }

    object UnexpectedFinish : UpdateDefaultRoundsState() {
        override fun asDisplayString(resources: Resources) =
                resources.getString(R.string.err_about__update_default_rounds_failed)
    }

    enum class CompletionType { ALREADY_UP_TO_DATE, COMPLETE }
    enum class ErrorType { CANT_ACQUIRE_LOCK }
}
