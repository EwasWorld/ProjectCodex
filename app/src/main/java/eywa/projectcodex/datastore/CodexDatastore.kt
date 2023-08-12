package eywa.projectcodex.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Helper function for getting the value of a specific [key] out of the map returned by [CodexDatastore.get]
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Map<DatastoreKey<*>, *>.retrieve(key: DatastoreKey<T>) =
        (this[key] ?: throw NoSuchElementException()) as T

interface CodexDatastore {
    fun <T : Any> get(key: DatastoreKey<T>): Flow<T>

    /**
     * @see retrieve
     */
    fun get(keys: Collection<DatastoreKey<*>>): Flow<Map<DatastoreKey<*>, *>>
    fun get(vararg keys: DatastoreKey<*>): Flow<Map<DatastoreKey<*>, *>> = get(keys.toList())
    suspend fun <T : Any> set(key: DatastoreKey<T>, value: T)
    suspend fun toggle(key: DatastoreKey<Boolean>)
}
