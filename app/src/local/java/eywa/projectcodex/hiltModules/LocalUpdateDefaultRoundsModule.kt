package eywa.projectcodex.hiltModules

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTaskImpl
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.datastore.CodexDatastore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class LocalUpdateDefaultRoundsModule {
    @Singleton
    @Provides
    fun providesDefaultRoundsInfo(
            @ApplicationContext context: Context,
            db: ScoresRoomDatabase,
            datastore: CodexDatastore,
            logging: CustomLogger,
    ): UpdateDefaultRoundsTask {
        return if (useActual) {
            UpdateDefaultRoundsTaskImpl(RoundRepo(db), context.resources, datastore, logging)
        }
        else {
            FakeUpdateDefaultRoundsTask().apply { mockedTask = this }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak") // It's a mock so contents isn't used
        var mockedTask: FakeUpdateDefaultRoundsTask? = null
            private set
        var useActual = false
    }
}

/**
 * TODO Struggling to get mockito to work with the suspend function :(
 */
class FakeUpdateDefaultRoundsTask : UpdateDefaultRoundsTask {
    var runTaskFakeReturn = true
    var stateFakeReturn = UpdateDefaultRoundsStatePreviewHelper.complete

    var runTaskCalls = 0
        private set
    var stateCalls = 0
        private set

    override val state: StateFlow<UpdateDefaultRoundsState>
        get() {
            stateCalls++
            return MutableStateFlow(stateFakeReturn)
        }

    override suspend fun runTask(): Boolean {
        Log.i("UpdateFakeTest", "Run called")
        runTaskCalls++
        return runTaskFakeReturn
    }
}
