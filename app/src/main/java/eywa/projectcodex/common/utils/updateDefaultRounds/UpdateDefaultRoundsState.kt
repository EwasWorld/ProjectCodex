package eywa.projectcodex.common.utils.updateDefaultRounds

import android.content.res.Resources
import eywa.projectcodex.R

sealed class UpdateDefaultRoundsState {
    abstract val databaseVersion: Int?

    object Initialising : UpdateDefaultRoundsState() {
        override val databaseVersion: Int? = null
    }

    /**
     * @param currentItemIndex 1-indexed
     */
    data class StartProcessingNew(
            override val databaseVersion: Int?, val currentItemIndex: Int, val totalItems: Int
    ) : UpdateDefaultRoundsState()

    data class DeletingOld(override val databaseVersion: Int?) : UpdateDefaultRoundsState()
    data class Complete(override val databaseVersion: Int?, val type: CompletionType) : UpdateDefaultRoundsState()
    data class InternalError(override val databaseVersion: Int?, val message: String) : UpdateDefaultRoundsState()
    data class TemporaryError(override val databaseVersion: Int?, val type: ErrorType) : UpdateDefaultRoundsState()

    enum class CompletionType { ALREADY_UP_TO_DATE, COMPLETE }
    enum class ErrorType { CANT_ACQUIRE_LOCK }
}

fun UpdateDefaultRoundsState?.asDisplayString(resources: Resources) = when (this) {
    null -> resources.getString(R.string.about__update_default_rounds_not_started)
    UpdateDefaultRoundsState.Initialising -> resources.getString(R.string.about__update_default_rounds_initialising)
    is UpdateDefaultRoundsState.StartProcessingNew -> resources.getString(
            R.string.about__update_default_rounds_progress, currentItemIndex, totalItems
    )
    is UpdateDefaultRoundsState.DeletingOld -> resources.getString(R.string.about__update_default_rounds_deleting)
    is UpdateDefaultRoundsState.Complete -> resources.getString(R.string.about__update_default_rounds_up_to_date)
    is UpdateDefaultRoundsState.InternalError -> resources.getString(R.string.err_about__update_default_rounds_failed)
    is UpdateDefaultRoundsState.TemporaryError -> when (type) {
        UpdateDefaultRoundsState.ErrorType.CANT_ACQUIRE_LOCK -> resources.getString(R.string.err_about__update_default_rounds_no_lock)
    }
}

internal fun UpdateDefaultRoundsState?.asLogString() = when (this) {
    null -> "Not started"
    is UpdateDefaultRoundsState.StartProcessingNew -> "Processing $currentItemIndex of $totalItems"
    else -> this::class.simpleName
}
