package eywa.projectcodex.datastore

import kotlinx.coroutines.flow.Flow

interface CodexDatastore {
    fun <T : Any> get(key: DatastoreKey<T>): Flow<T>
    fun <T : Any> get(keys: Collection<DatastoreKey<T>>): Flow<Map<DatastoreKey<T>, T>>
    suspend fun <T : Any> set(key: DatastoreKey<T>, value: T)
    suspend fun toggle(key: DatastoreKey<Boolean>)
}

