package eywa.projectcodex.components.referenceTables.rankingPoints

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
class RankingPointsModule {
    @Singleton
    @Provides
    fun providesRankingPoints(@ApplicationContext context: Context): RankingPointsUseCase {
        val rawString = context.resources
                .openRawResource(R.raw.national_ranking_points)
                .bufferedReader()
                .use { it.readText() }
        return RankingPointsUseCase(rawString)
    }
}
