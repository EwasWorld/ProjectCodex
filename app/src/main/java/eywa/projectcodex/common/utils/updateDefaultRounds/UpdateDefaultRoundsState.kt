package eywa.projectcodex.common.utils.updateDefaultRounds

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

sealed class UpdateDefaultRoundsState {
    open val databaseVersion: Int? = null
    open val hasTaskFinished = true
    abstract val displayString: ResOrActual<String>
    open fun asLogString() = this::class.simpleName

    object NotStarted : UpdateDefaultRoundsState() {
        override val hasTaskFinished = false

        override fun asLogString(): String = "Not started"

        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.about__update_default_rounds_not_started)
    }

    object Initialising : UpdateDefaultRoundsState() {
        override val databaseVersion: Int? = null
        override val hasTaskFinished = false
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.about__update_default_rounds_initialising)
    }

    /**
     * @param currentItemIndex 1-indexed
     */
    data class StartProcessingNew(
            override val databaseVersion: Int?, val currentItemIndex: Int, val totalItems: Int
    ) : UpdateDefaultRoundsState() {
        override val hasTaskFinished = false
        override fun asLogString(): String = "Processing $currentItemIndex of $totalItems"
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(
                    R.string.about__update_default_rounds_processing,
                    listOf(currentItemIndex, totalItems),
            )
    }

    data class DeletingOld(override val databaseVersion: Int?) : UpdateDefaultRoundsState() {
        override val hasTaskFinished = false
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.about__update_default_rounds_deleting)
    }

    data class Complete(override val databaseVersion: Int?, val type: CompletionType) : UpdateDefaultRoundsState() {
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.about__update_default_rounds_up_to_date)
    }

    data class InternalError(override val databaseVersion: Int?, val message: String) : UpdateDefaultRoundsState() {
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.err_about__update_default_rounds_failed)
    }

    data class TemporaryError(override val databaseVersion: Int?, val type: ErrorType) : UpdateDefaultRoundsState() {
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(
                    when (type) {
                        ErrorType.CANT_ACQUIRE_LOCK -> R.string.err_about__update_default_rounds_no_lock
                    }
            )
    }

    object UnexpectedFinish : UpdateDefaultRoundsState() {
        override val displayString: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.err_about__update_default_rounds_failed)
    }

    enum class CompletionType { ALREADY_UP_TO_DATE, COMPLETE }
    enum class ErrorType { CANT_ACQUIRE_LOCK }
}

object UpdateDefaultRoundsStatePreviewHelper {
    val complete = UpdateDefaultRoundsState
            .Complete(1, UpdateDefaultRoundsState.CompletionType.ALREADY_UP_TO_DATE)
}
