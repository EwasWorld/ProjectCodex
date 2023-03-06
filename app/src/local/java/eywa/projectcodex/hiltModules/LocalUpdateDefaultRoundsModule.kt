package eywa.projectcodex.hiltModules

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.CompletionType
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundRepo
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
            sharedPreferences: SharedPreferences,
            logging: CustomLogger,
    ): UpdateDefaultRoundsTask {
        return if (useActual) {
            UpdateDefaultRoundsTask(RoundRepo(db), context.resources, sharedPreferences, logging)
        }
        else {
            FakeUpdateDefaultRoundsTask(RoundRepo(db), context, sharedPreferences, logging)
                    .apply { mockedTask = this }
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
class FakeUpdateDefaultRoundsTask(
        repository: RoundRepo,
        context: Context,
        sharedPreferences: SharedPreferences,
        customLogger: CustomLogger,
) : UpdateDefaultRoundsTask(repository, context.resources, sharedPreferences, customLogger) {
    var runTaskFakeReturn = true
    var stateFakeReturn = UpdateDefaultRoundsState.Complete(1, CompletionType.ALREADY_UP_TO_DATE)

    var runTaskCalls = 0
        private set
    var stateCalls = 0
        private set

    override val state: StateFlow<UpdateDefaultRoundsState?>
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
