package eywa.projectcodex.viewModels

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
    private val repository: RoundsRepo
    val rounds: LiveData<List<Round>>
    val roundArrowCounts: LiveData<List<RoundArrowCount>>
    val roundDistances: LiveData<List<RoundDistance>>
    val roundSubTypes: LiveData<List<RoundSubType>>
    val updateDefaultRoundsState = UpdateDefaultRounds.getState()
    val updateDefaultRoundsProgressMessage = UpdateDefaultRounds.getProgressMessage()

    init {
        val db = ScoresRoomDatabase.getDatabase(application)

        repository = RoundsRepo(db)
        rounds = repository.rounds
        roundArrowCounts = repository.roundArrowCounts
        roundDistances = repository.roundDistances
        roundSubTypes = repository.roundSubTypes
    }

    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources) {
        UpdateDefaultRounds.runUpdate(getApplication() as Application, resources)
    }

    /**
     * @see UpdateDefaultRounds.cancelUpdateDefaultRounds
     */
    fun cancelUpdateDefaultRounds() {
        UpdateDefaultRounds.cancelUpdateDefaultRounds()
    }

    /**
     * @see UpdateDefaultRounds.resetState
     */
    fun resetUpdateDefaultRoundsStateIfComplete() {
        UpdateDefaultRounds.resetState()
    }
}