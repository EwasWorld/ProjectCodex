package eywa.projectcodex.testUtils

import androidx.annotation.VisibleForTesting
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

/**
 * In the main package so it can be used in unit tests and instrumented tests. Maybe there's a better spot for it?
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class MockDatastore {
    var values: Map<DatastoreKey<out Any>, Any> = emptyMap()
    var valuesDelayed: Map<DatastoreKey<out Any>, Any>? = null

    val mock: CodexDatastore = mock {
        on { get<Any>(key = any()) } doAnswer {
            flow {
                val key = it.arguments.first() as DatastoreKey<*>
                val firstEmission = values[key] ?: key.defaultValue
                emit(firstEmission)

                valuesDelayed.takeIf { !it.isNullOrEmpty() }?.let {
                    val secondEmission = it[key] ?: key.defaultValue
                    if (secondEmission != firstEmission) {
                        delay(TestUtils.FLOW_EMIT_DELAY)
                        emit(secondEmission)
                    }
                }
            }
        }
        on { get<Any>(keys = any()) } doAnswer {
            @Suppress("UNCHECKED_CAST")
            flow {
                fun getEmission(keys: Collection<DatastoreKey<*>>, keyValues: Map<DatastoreKey<out Any>, Any>) =
                        keys.associateWith { keyValues[it] ?: it.defaultValue } as Map<DatastoreKey<Any>, Any>

                val keys = it.arguments.first() as Collection<DatastoreKey<*>>
                val firstEmission = getEmission(keys, values)
                emit(firstEmission)

                valuesDelayed.takeIf { !it.isNullOrEmpty() }?.let {
                    val secondEmission = getEmission(keys, it)
                    if (secondEmission != firstEmission) {
                        delay(TestUtils.FLOW_EMIT_DELAY)
                        emit(secondEmission)
                    }
                }
            }
        }
    }
}
