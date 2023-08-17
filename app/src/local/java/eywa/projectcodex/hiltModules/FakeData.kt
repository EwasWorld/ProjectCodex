package eywa.projectcodex.hiltModules

import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Qualifier

interface FakeData {
    suspend fun addFakeData(db: ScoresRoomDatabase)
}

@Qualifier
annotation class FakeDataAnnotation

@Module
@InstallIn(SingletonComponent::class)
abstract class FakeDataMainModule {
    @BindsOptionalOf
    @FakeDataAnnotation
    abstract fun bindFakeData(): FakeData
}
