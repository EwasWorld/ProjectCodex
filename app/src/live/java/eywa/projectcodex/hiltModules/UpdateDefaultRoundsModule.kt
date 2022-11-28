package eywa.projectcodex.hiltModules

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundRepo
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UpdateDefaultRoundsModule {
    @Singleton
    @Provides
    fun providesDefaultRoundsInfo(
            @ApplicationContext context: Context,
            db: ScoresRoomDatabase,
            sharedPreferences: SharedPreferences,
            logging: CustomLogger,
    ) = UpdateDefaultRoundsTask(RoundRepo(db), context.resources, sharedPreferences, logging)
}