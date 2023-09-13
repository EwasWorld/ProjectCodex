package eywa.projectcodex.common.utils.classificationTables

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.R
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClassificationModule {
    @Singleton
    @Provides
    fun providesClassificationTables(@ApplicationContext context: Context): ClassificationTablesUseCase {
        val rawString = context.resources
                .openRawResource(R.raw.classification_round_scores_2023)
                .bufferedReader()
                .use { it.readText() }
        return ClassificationTablesUseCase(rawString)
    }
}
