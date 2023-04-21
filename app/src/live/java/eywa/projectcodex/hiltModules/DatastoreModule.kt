package eywa.projectcodex.hiltModules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.CodexDatastoreImpl
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "codex_user_preferences"

@InstallIn(SingletonComponent::class)
@Module
object DatastoreModule {
    @Singleton
    @Provides
    fun provideCodexDatastore(@ApplicationContext context: Context): CodexDatastore {
        val datastore = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(USER_PREFERENCES_NAME) }
        return CodexDatastoreImpl(datastore)
    }
}
