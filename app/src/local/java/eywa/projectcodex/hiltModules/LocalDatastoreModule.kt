package eywa.projectcodex.hiltModules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocalDatastoreModule {
    var datastore = FakeDatastore()
        private set

    @Singleton
    @Provides
    fun provideCodexDatastore(): CodexDatastore = datastore

    fun teardown() {
        datastore = FakeDatastore()
    }
}


class FakeDatastore : CodexDatastore {
    private var values = MutableStateFlow(
            mapOf<DatastoreKey<out Any>, Any>(DatastoreKey.DisplayHandicapNotice to false)
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: DatastoreKey<T>): Flow<T> =
            values.map { (it[key] as? T) ?: key.defaultValue }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(keys: Collection<DatastoreKey<T>>): Flow<Map<DatastoreKey<T>, T>> =
            values.map { keys.associateWith { key -> (it[key] as? T) ?: key.defaultValue } }

    override suspend fun <T : Any> set(key: DatastoreKey<T>, value: T) {
        values.update { it.plus(key to value) }
    }

    override suspend fun toggle(key: DatastoreKey<Boolean>) {
        values.update {
            val previous = (it[key] as? Boolean) ?: key.defaultValue
            it.plus(key to !previous)
        }
    }

    fun setValues(valuesMap: Map<DatastoreKey<out Any>, Any>) {
        values.update { valuesMap }
    }
}
