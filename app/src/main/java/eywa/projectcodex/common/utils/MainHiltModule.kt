package eywa.projectcodex.common.utils

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.common.utils.SharedPrefs.Companion.getSharedPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainHiltModule {
    @Singleton
    @Provides
    fun providesSharedPreferences(
            @ApplicationContext context: Context,
    ) = context.getSharedPreferences()
}
