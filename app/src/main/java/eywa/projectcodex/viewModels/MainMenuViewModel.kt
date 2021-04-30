package eywa.projectcodex.viewModels

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.database.repositories.RoundsRepo
import eywa.projectcodex.logic.UpdateDefaultRounds

/**
 * @see InputEndViewModel
 */
class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val LOG_TAG = "MainMenuVM"
    }

    private val repository: RoundsRepo
    val rounds: LiveData<List<Round>>
    val roundArrowCounts: LiveData<List<RoundArrowCount>>
    val roundDistances: LiveData<List<RoundDistance>>
    val roundSubTypes: LiveData<List<RoundSubType>>
    val updateDefaultRoundsProgress = MutableLiveData<String?>(null)
    private val taskExecutor: TaskRunner by lazy { TaskRunner() }

    // TODO Will this be lost when navigating away?
    private var currentTask: TaskRunner.ProgressTask<String, Void?>? = null

    init {
        val db = ScoresRoomDatabase.getDatabase(application, viewModelScope)

        repository = RoundsRepo(db)
        rounds = repository.rounds
        roundArrowCounts = repository.roundArrowCounts
        roundDistances = repository.roundDistances
        roundSubTypes = repository.roundSubTypes
    }

    fun updateDefaultRounds(resources: Resources) {
        synchronized(updateDefaultRoundsProgress) {
            if (updateDefaultRoundsProgress.value != null) return
            updateDefaultRoundsProgress.postValue(
                    resources.getString(R.string.main_menu__update_default_rounds_progress_init)
            )
        }
        currentTask = UpdateDefaultRounds(repository, resources, viewModelScope)
        taskExecutor.executeProgressTask(
                currentTask!!,
                onProgress = { progress -> updateDefaultRoundsProgress.postValue(progress) },
                onComplete = {
                    currentTask = null
                    updateDefaultRoundsProgress.postValue(null)
                },
                onError = { exception ->
                    CustomLogger.customLogger.e(
                            LOG_TAG,
                            "Update default rounds task failed with exception: " + exception.message
                    )
                    currentTask = null
                    updateDefaultRoundsProgress.postValue(null)
                }
        )
    }

    fun cancelUpdateDefaultRounds() {
        currentTask?.isSoftCancelled = true
    }
}