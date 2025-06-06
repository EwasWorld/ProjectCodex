package eywa.projectcodex.hiltModules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTaskImpl
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.datastore.CodexDatastore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UpdateDefaultRoundsModule {
    @Singleton
    @Provides
    fun providesDefaultRoundsInfo(
            @ApplicationContext context: Context,
            db: ScoresRoomDatabase,
            datastore: CodexDatastore,
            logging: CustomLogger,
    ): UpdateDefaultRoundsTask = UpdateDefaultRoundsTaskImpl(db.roundsRepo(), context.resources, datastore, logging)
}
