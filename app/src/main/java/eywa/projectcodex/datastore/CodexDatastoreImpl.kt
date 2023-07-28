package eywa.projectcodex.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CodexDatastoreImpl(private val datastore: DataStore<Preferences>) : CodexDatastore {
    override fun <T : Any> get(key: DatastoreKey<T>) =
            datastore.data.map { prefs -> prefs[key.key] ?: key.defaultValue }

    override fun get(keys: Collection<DatastoreKey<*>>): Flow<Map<DatastoreKey<*>, *>> =
            datastore.data.map { prefs -> keys.associateWith { prefs[it.key] ?: it.defaultValue } }

    override suspend fun <T : Any> set(key: DatastoreKey<T>, value: T) {
        datastore.edit { it[key.key] = value }
    }

    override suspend fun toggle(key: DatastoreKey<Boolean>) {
        datastore.edit {
            val currentValue = it[key.key] ?: key.defaultValue
            it[key.key] = !currentValue
        }
    }
}
